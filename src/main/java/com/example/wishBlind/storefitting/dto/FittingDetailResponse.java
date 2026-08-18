package com.example.wishBlind.storefitting.dto;

import com.example.wishBlind.recommendation.domain.Recommendation;
import com.example.wishBlind.storefitting.domain.FittingResult;
import com.example.wishBlind.storefitting.domain.StoreFitting;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "매장 체험 예약 상세")
public record FittingDetailResponse(
        Long id,
        String customerName,
        String brand,
        String reservationNumber,
        LocalDate reserveDate,
        LocalTime reserveTime,
        String status,
        String statusLabel,
        String category,
        String purpose,
        @Schema(description = "체험할 후보 상품(AI 추천)") List<Candidate> candidates,
        @Schema(description = "체험 항목") List<String> experienceItems,
        @Schema(description = "체험 결과(완료 시)") ResultDto result
) {

    @Schema(description = "체험 후보")
    public record Candidate(Long productId, String productName, boolean best, int rank) {
        static Candidate from(Recommendation r) {
            return new Candidate(r.getProduct().getId(), r.getProduct().getName(), r.isBest(), r.getRank());
        }
    }

    @Schema(description = "체험 결과")
    public record ResultDto(Long preferredCandidateProductId, String materialFeel, String sizeFeel,
                            String storageFeel, String wearComfort, String weight,
                            String overallSatisfaction, String staffMemo) {
        static ResultDto from(FittingResult r) {
            if (r == null) return null;
            return new ResultDto(r.getPreferredCandidateProductId(), r.getMaterialFeel(), r.getSizeFeel(),
                    r.getStorageFeel(), r.getWearComfort(), r.getWeight(), r.getOverallSatisfaction(), r.getStaffMemo());
        }
    }

    private static final List<String> EXPERIENCE_ITEMS = List.of("소재", "크기", "무게", "착용감");

    public static FittingDetailResponse of(StoreFitting f, List<Recommendation> candidates) {
        return new FittingDetailResponse(
                f.getId(),
                f.getCustomerName(),
                f.getBrand(),
                f.getReservationNumber(),
                f.getReserveDate(),
                f.getReserveTime(),
                f.getStatus().name(),
                f.getStatus().getLabel(),
                f.getGiftSession().getCategory(),
                f.getGiftSession().getOccasion(),
                candidates.stream().map(Candidate::from).toList(),
                EXPERIENCE_ITEMS,
                ResultDto.from(f.getResult())
        );
    }
}
