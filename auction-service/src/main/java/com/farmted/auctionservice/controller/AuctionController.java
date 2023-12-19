package com.farmted.auctionservice.controller;

import com.farmted.auctionservice.dto.responseAuctionDto.AuctionBuyerResponseDto;
import com.farmted.auctionservice.dto.responseAuctionDto.AuctionGetResponseDto;
import com.farmted.auctionservice.dto.responseAuctionDto.AuctionSellerResponseDto;
import com.farmted.auctionservice.service.AuctionService;
import com.farmted.auctionservice.util.GlobalResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("auction-service")
public class AuctionController {

    private final AuctionService auctionService;


// 판매자 -> 낙찰 내역 조회 -> memberUuid
    @GetMapping("/seller/{memberUuid}/board")
    public ResponseEntity<?> findAuctionToSeller(
            @PathVariable String memberUuid
    ){
        List<AuctionBuyerResponseDto> auctionBuyerList = auctionService.auctionBuyerList(memberUuid);
        return ResponseEntity.ok(GlobalResponseDto.listOf(auctionBuyerList));
    }

// 구매자  ->  낙찰 내역 조회 -> auctionBuyer
    @GetMapping("/{auctionBuyer}/board")
        public ResponseEntity<?> findAuctionTrue(
                @PathVariable String auctionBuyer
    ){
        List<AuctionSellerResponseDto> auctionSellerList = auctionService.auctionTrueList(auctionBuyer);
        return ResponseEntity.ok(GlobalResponseDto.listOf(auctionSellerList));
    }

// 경매 내역 상세 조회
    @GetMapping("/auction/{boardUuid}/board")
    public ResponseEntity<?> getAuctionDetail(@PathVariable String boardUuid){
        AuctionGetResponseDto auction = auctionService.getAuctionDetail(boardUuid);
        return ResponseEntity.ok(GlobalResponseDto.of(auction));
    }
}
