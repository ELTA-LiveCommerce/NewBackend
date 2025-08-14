package kr.elta.backend.util;

import kr.elta.backend.entity.PointTransactionEntity;
import kr.elta.backend.repository.PointTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PointTransactionUtil {
    private static PointTransactionRepository pointTransactionRepository;
    
    @Autowired
    public void setPointTransactionRepository(PointTransactionRepository pointTransactionRepository) {
        PointTransactionUtil.pointTransactionRepository = pointTransactionRepository;
    }
    
    public static void recordTransaction(Long sellerUuid, double price, String description) {
        PointTransactionEntity transaction = PointTransactionEntity.builder()
                .sellerUuid(sellerUuid)
                .price(Math.abs(price))
                .isPlus(price > 0)
                .description(description)
                .build();
        
        pointTransactionRepository.save(transaction);
    }
}