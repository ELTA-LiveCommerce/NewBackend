package kr.elta.backend.util;

import com.popbill.api.KakaoService;
import com.popbill.api.PopbillException;
import com.popbill.api.kakao.KakaoButton;
import com.popbill.api.kakao.KakaoReceiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class KakaoNotificationUtil {
    
    @Autowired
    private KakaoService kakaoService;
    
    @Value("${popbill.corpNum}")
    private String corpNum;
    
    @Value("${popbill.userId}")
    private String userId;
    
    /**
     * 카카오 알림톡(ATS) 전송
     * 
     * @param templateCode 템플릿 코드 (카카오 사전 승인 필요)
     * @param content 알림톡 내용
     * @param altSubject 대체문자 제목
     * @param altContent 대체문자 내용
     * @param receiverNum 수신번호
     * @param receiverName 수신자명
     * @return 전송 성공 여부
     */
    public boolean sendATS(String templateCode, String content, String altSubject, 
                          String altContent, String receiverNum, String receiverName) {
        return sendATS(templateCode, content, altSubject, altContent, receiverNum, receiverName, "01077497206");
    }
    
    /**
     * 카카오 알림톡(ATS) 전송 (발신번호 지정)
     * 
     * @param templateCode 템플릿 코드 (카카오 사전 승인 필요)
     * @param content 알림톡 내용
     * @param altSubject 대체문자 제목
     * @param altContent 대체문자 내용
     * @param receiverNum 수신번호
     * @param receiverName 수신자명
     * @param senderNum 발신번호 (등록된 번호)
     * @return 전송 성공 여부
     */
    public boolean sendATS(String templateCode, String content, String altSubject, 
                          String altContent, String receiverNum, String receiverName, String senderNum) {
        try {
            String receiptNum = kakaoService.sendATS(
                corpNum,  // 사업자번호
                templateCode, // 템플릿 코드
                senderNum, // 발신번호
                content, // 알림톡 내용
                altSubject, // 대체문자 제목
                altContent, // 대체문자 내용
                "C", // 대체문자 전송타입 (C: 알림톡과 동일)
                receiverNum, // 수신번호
                receiverName, // 수신자명
                "", // 예약전송일시 (즉시전송)
                userId, // 팝빌 회원 아이디
                "ATS_" + System.currentTimeMillis() // 전송요청번호
            );
            return true;
        } catch (PopbillException e) {
            log.error("Failed to send SendATS notification. Template: {}, Receiver: {}, Error: {}", 
                    templateCode, receiverNum, e.getMessage());
            return false;
        }
    }
    
    /**
     * 카카오 알림톡 대량전송 (SendATSMulti)
     * 
     * @param templateCode 템플릿 코드 (카카오 사전 승인 필요)
     * @param senderNum 발신번호 (등록된 번호)
     * @param content 알림톡 내용
     * @param altSubject 대체문자 제목
     * @param altContent 대체문자 내용
     * @param receivers 수신자 목록
     * @return 전송 성공 여부
     */
    public boolean sendATSMulti(String templateCode, String senderNum, String content, 
                               String altSubject, String altContent, List<KakaoReceiver> receivers) {
        try {
            String receiptNum = kakaoService.sendATS(
                corpNum,  // 사업자번호
                templateCode, // 템플릿 코드
                senderNum, // 발신번호
                content, // 알림톡 내용
                altSubject, // 대체문자 제목
                altContent, // 대체문자 내용
                "C", // 대체문자 전송타입 (C: 알림톡과 동일)
                receivers.toArray(new KakaoReceiver[0]), // 수신자 배열
                "", // 예약전송일시 (즉시전송)
                userId, // 팝빌 회원 아이디
                "ATS_MULTI_" + System.currentTimeMillis() // 전송요청번호
            );
            
            log.info("대량 알림톡 전송 성공 - 접수번호: {}, 수신자 수: {}", receiptNum, receivers.size());
            return true;
        } catch (PopbillException e) {
            log.error("Failed to send SendATSMulti notification. Template: {}, Receivers: {}, Error: {}", 
                    templateCode, receivers.size(), e.getMessage());
            return false;
        }
    }
    
    /**
     * 방송 시작 알림톡 대량전송 (특화 메서드)
     * 
     * @param sellerName 판매자 이름
     * @param sellerUrlName 판매자 URL명
     * @param followers 팔로워 목록 (전화번호, 이름 포함)
     * @return 전송 성공 여부
     */
    public boolean sendBroadcastStartNotificationMulti(String sellerName, String sellerUrlName, List<FollowerInfo> followers) {
        if (followers == null || followers.isEmpty()) {
            log.warn("팔로워 목록이 비어있어 방송 시작 알림을 전송하지 않습니다.");
            return true;
        }
        
        String content = String.format("[%s 라이브 시작]\n\n신청하신 라이브가 시작되었어요!\n인원이 다 차기 전에 빨리 입장해 주세요!\n\n이 메시지는 해당 라이브 알림을 신청하신 고객님께 발송됩니다.", sellerName);
        
        // 템플릿 변수 치환을 위한 실제 URL
        String profileUrl = String.format("https://elta.kr/#/sellers/%s", sellerUrlName);
        
        // KakaoReceiver 리스트 생성 (버튼 포함)
        List<KakaoReceiver> receivers = followers.stream()
            .filter(follower -> follower.getPhoneNum() != null && !follower.getPhoneNum().trim().isEmpty())
            .map(follower -> {
                KakaoReceiver receiver = new KakaoReceiver();
                receiver.setReceiverNum(follower.getPhoneNum());
                receiver.setReceiverName(follower.getName() != null ? follower.getName() : "고객");
                
                // 버튼 설정 (셀러프로필)
                if (sellerUrlName != null && !sellerUrlName.trim().isEmpty()) {
                    KakaoButton button = new KakaoButton();
                    button.setN("셀러프로필");  // 버튼명
                    button.setT("WL");        // 버튼타입 (WL: 웹링크)
                    button.setU1(profileUrl); // 모바일 웹 URL
                    button.setU2(profileUrl); // PC 웹 URL
                    button.setTg("out");      // 타겟 (out = 외부 브라우저)
                    
                    receiver.setBtns(Arrays.asList(button));
                }
                
                return receiver;
            })
            .toList();
        
        if (receivers.isEmpty()) {
            log.warn("유효한 전화번호를 가진 팔로워가 없어 방송 시작 알림을 전송하지 않습니다.");
            return true;
        }
        
        return sendATSMulti(
            "025070000989", // 방송 시작 알림 템플릿 코드
            "01077497206", // 발신번호
            content,
            "방송 시작 알림",
            sellerName + "님의 라이브 방송이 시작되었습니다!",
            receivers
        );
    }
    
    /**
     * 팔로우 알림톡 전송 (특화 메서드)
     * 
     * @param followerName 팔로워 이름
     * @param sellerName 판매자 이름
     * @param sellerPhone 판매자 전화번호
     * @return 전송 성공 여부
     */
    public boolean sendFollowNotification(String followerName, String sellerName, String sellerPhone) {
        String content = "[팔로우 알림]\n\n" +
                        followerName + "님이 " + sellerName + "님을 팔로우 했습니다.\n\n" +
                        "해당 알림은 구매자들이 셀러님의 프로필 페이지에서 '팔로우 버튼'을 누르면 수신되는 메세지이며 요청하신 해당 조건이 있을 경우 발송됩니다.";
        
        return sendATS(
            "025070000625", // 팔로우 알림 템플릿 코드
            content,
            "팔로우 알림",
            "새로운 팔로워가 생겼습니다!",
            sellerPhone,
            sellerName
        );
    }
    
    /**
     * 방송 종료 후 정산서 알림톡 발송 (특화 메서드)
     * 
     * @param broadcastTitle 방송 제목
     * @param bankName 은행명
     * @param accountNum 계좌번호
     * @param accountHolder 예금주
     * @param totalAmount 총 입금액
     * @param buyerName 구매자명
     * @param buyerPhone 구매자 전화번호
     * @param orderUuid 주문 UUID
     * @param sellerUrlName 판매자 URL명
     * @return 전송 성공 여부
     */
    public boolean sendReconciliationNotification(String broadcastTitle, String bankName, String accountNum, 
                                                 String accountHolder, int totalAmount, String buyerName, 
                                                 String buyerPhone, Long orderUuid, String sellerUrlName) {
        if (buyerPhone == null || buyerPhone.trim().isEmpty()) {
            log.warn("구매자 전화번호가 없어 정산서 알림을 전송하지 않습니다.");
            return false;
        }
        
        String content = String.format(
            "[주문 입금 안내]\n\n" +
            "%s 정산서가 도착했어요!\n" +
            "아래 계좌로 1시간 이내 꼭 입금해 주세요.\n" +
            "• 입금계좌 : %s / %s / %s\n" +
            "• 입금액 : %,d원\n" +
            "• 입금자명 : %s으로 미입금시 거래파기 자동 등록되어 추후 주문이 불가 할 수 있으니 필히 확인 부탁드립니다.\n" +
            "감사합니다.",
            broadcastTitle, bankName, accountNum, accountHolder, totalAmount, buyerName
        );
        
        try {
            KakaoReceiver receiver = new KakaoReceiver();
            receiver.setReceiverNum(buyerPhone);
            receiver.setReceiverName(buyerName != null ? buyerName : "고객");
            
            // 버튼 설정
            List<KakaoButton> buttons = new ArrayList<>();
            
            // 정산서보기 버튼
            KakaoButton reconciliationButton = new KakaoButton();
            reconciliationButton.setN("정산서보기");
            reconciliationButton.setT("WL");
            reconciliationButton.setU1(String.format("https://elta.kr/#/reconciliation/%d", orderUuid));
            reconciliationButton.setU2(String.format("https://elta.kr/#/reconciliation/%d", orderUuid));
            reconciliationButton.setTg("out");
            buttons.add(reconciliationButton);
            
            // 셀러프로필 버튼
            if (sellerUrlName != null && !sellerUrlName.trim().isEmpty()) {
                KakaoButton sellerButton = new KakaoButton();
                sellerButton.setN("셀러프로필");
                sellerButton.setT("WL");
                sellerButton.setU1(String.format("https://elta.kr/#/sellers/%s", sellerUrlName));
                sellerButton.setU2(String.format("https://elta.kr/#/sellers/%s", sellerUrlName));
                sellerButton.setTg("out");
                buttons.add(sellerButton);
            }
            
            receiver.setBtns(buttons);
            
            String receiptNum = kakaoService.sendATS(
                corpNum,
                "025070000990", // 정산서 알림 템플릿 코드
                "01077497206", // 발신번호
                content,
                "주문 입금 안내",
                String.format("%s님의 주문 입금 안내입니다.", buyerName),
                "C",
                new KakaoReceiver[]{receiver},
                "",
                userId,
                "RECONCILIATION_" + System.currentTimeMillis()
            );
            
            log.info("정산서 알림톡 전송 성공 - 구매자: {}, 주문: {}, 금액: {}원", buyerName, orderUuid, totalAmount);
            return true;
            
        } catch (PopbillException e) {
            log.error("정산서 알림톡 전송 실패 - 구매자: {}, 주문: {}, 오류: {}", buyerName, orderUuid, e.getMessage());
            return false;
        }
    }
    
    /**
     * 비밀번호 찾기 임시 비밀번호 알림톡 전송 (특화 메서드)
     * 
     * @param tempPassword 임시 비밀번호
     * @param userPhone 사용자 전화번호
     * @param userName 사용자 이름
     * @return 전송 성공 여부
     */
    public boolean sendPasswordResetNotification(String tempPassword, String userPhone, String userName) {
        if (userPhone == null || userPhone.trim().isEmpty()) {
            log.warn("사용자 전화번호가 없어 비밀번호 재설정 알림을 전송하지 않습니다.");
            return false;
        }
        
        String content = String.format(
            "[계정 정보 안내]\n\n" +
            "요청하신 계정 정보를 안내드립니다.\n" +
            "- 임시 비밀번호: %s\n\n" +
            "보안을 위해 로그인 후 반드시 비밀번호를 변경해 주세요",
            tempPassword
        );
        
        return sendATS(
            "025070000637", // 비밀번호 재설정 알림 템플릿 코드
            content,
            "계정 정보 안내",
            String.format("%s님의 임시 비밀번호가 발급되었습니다.", userName != null ? userName : "고객"),
            userPhone,
            userName != null ? userName : "고객"
        );
    }
    
    /**
     * 팔로워 정보를 담는 내부 클래스
     */
    public static class FollowerInfo {
        private String phoneNum;
        private String name;
        
        public FollowerInfo(String phoneNum, String name) {
            this.phoneNum = phoneNum;
            this.name = name;
        }
        
        public String getPhoneNum() { return phoneNum; }
        public String getName() { return name; }
    }
}