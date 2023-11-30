package com.farmted.boardservice.repository;

import com.farmted.boardservice.dto.request.RequestCreateBoardDto;
import com.farmted.boardservice.dto.response.listDomain.ResponseGetBoardDto;
import com.farmted.boardservice.enums.BoardType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("JPQL 테스트 코드")
@Transactional
public class JPQLTest {
    private final BoardRepository boardRepository;

    @Autowired
    public JPQLTest(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    // 레포 초기화 및 더미데이터 생성
    @BeforeEach
    public void beforeEach(){
        boardRepository.deleteAll();
        // 경매 생성
        boardRepository.save(new RequestCreateBoardDto(
                                    BoardType.AUCTION,
                                    "Auction Content",
                                    "Auction Title",
                                    "Auction Product",
                                    10,
                                    100,
                                    "Auction Source",
                                    "Auction Image"
                            ).toBoard("uuid"));
        // 판매 생성
        boardRepository.save(new RequestCreateBoardDto(
                                BoardType.SALE,
                                "Sale Content",
                                "Sale Title",
                                "Sale Product",
                                20,
                                200,
                                "Sale Source",
                                "Sale Image"
                        ).toBoard("uuid"));
        // 구매요청 생성
        boardRepository.save(new RequestCreateBoardDto(
                BoardType.COMMISSION,
                "Commission Content",
                "Commission Title",
                "",0,0,"",""
        ).toBoard("uuid"));
        // 그 이외의 더미데이터 (고객센터로 고정)
        IntStream.rangeClosed(1, 5).forEach( (i)->{
                boardRepository.save(new RequestCreateBoardDto(
                    BoardType.CUSTOMER_SERVICE,      // BoardType 값
                    "게시글 내용"+i,                  // 게시글 내용
                    "게시글 제목"+i,                  // 게시글 제목
                    "상품 이름"+i,                    // 상품 이름
                    10*i,                             // 상품 재고
                    10_000L*i,                         // 상품 가격
                    "상품 출처"+i,                    // 상품 출처
                    "상품 이미지 URL"+i               // 상품 이미지 URL
                ).toBoard("DummuUuid"+i));
            }
        );
    }

    @Test
    @DisplayName("Product(판매+경매) 게시글 조회")
    public void GetProductTest(){
        // given
        // when
        Page<ResponseGetBoardDto> boardDTO = boardRepository.findByBoardType(BoardType.PRODUCT,
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt")));
        // then
        assertThat(boardDTO.getContent().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("카테고리별 게시글 조회")
    public void GetCategoryTest(){
        // given
        // when
        Page<ResponseGetBoardDto> boardDTO = boardRepository.findByBoardType(BoardType.COMMISSION,
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt")));
        // then
        assertThat(boardDTO.getContent().size()).isEqualTo(1);
        assertThat(boardDTO.getContent().get(0).getBoardType()).isEqualTo(BoardType.COMMISSION);
    }

    @Test
    @DisplayName("Product 특정 회원의 게시글 리스트")
    public void GetMemberProduct(){
        // given
        // when
            // 회원UUID가 uuid인 게시글
        Page<ResponseGetBoardDto> boardDTO = boardRepository.findByMemberUuidAndBoardType("uuid", BoardType.PRODUCT,
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt")));
        // then
        assertThat(boardDTO.getContent().size()).isEqualTo(2);
    }
}
