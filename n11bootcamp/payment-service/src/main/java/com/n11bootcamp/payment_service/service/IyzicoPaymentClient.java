package com.n11bootcamp.payment_service.service;

import com.iyzipay.Options;
import com.iyzipay.model.Address;
import com.iyzipay.model.BasketItem;
import com.iyzipay.model.BasketItemType;
import com.iyzipay.model.Buyer;
import com.iyzipay.model.Currency;
import com.iyzipay.model.Locale;
import com.iyzipay.model.Payment;
import com.iyzipay.model.PaymentCard;
import com.iyzipay.model.PaymentChannel;
import com.iyzipay.model.PaymentGroup;
import com.iyzipay.request.CreatePaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class IyzicoPaymentClient {

    private static final Logger log = LoggerFactory.getLogger(IyzicoPaymentClient.class);

    private final String apiKey;
    private final String secretKey;
    private final String baseUrl;
    private final String cardHolderName;
    private final String cardNumber;
    private final String expireMonth;
    private final String expireYear;
    private final String cvc;

    public IyzicoPaymentClient(@Value("${iyzico.api-key:}") String apiKey,
                               @Value("${iyzico.secret-key:}") String secretKey,
                               @Value("${iyzico.base-url:https://sandbox-api.iyzipay.com}") String baseUrl,
                               @Value("${iyzico.test-card.holder-name:John Doe}") String cardHolderName,
                               @Value("${iyzico.test-card.number:5528790000000008}") String cardNumber,
                               @Value("${iyzico.test-card.expire-month:12}") String expireMonth,
                               @Value("${iyzico.test-card.expire-year:2030}") String expireYear,
                               @Value("${iyzico.test-card.cvc:123}") String cvc) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.baseUrl = baseUrl;
        this.cardHolderName = cardHolderName;
        this.cardNumber = cardNumber;
        this.expireMonth = expireMonth;
        this.expireYear = expireYear;
        this.cvc = cvc;
    }

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey) && StringUtils.hasText(secretKey);
    }

    public IyzicoPaymentResult pay(Long orderId,
                                   String username,
                                   List<Map<String, Object>> items,
                                   Map<String, Object> paymentCardPayload) {
        if (!isConfigured()) {
            return new IyzicoPaymentResult(false, "Iyzico is not configured");
        }

        BigDecimal totalPrice = calculateTotal(items);
        CreatePaymentRequest request = createRequest(orderId, username, items, totalPrice, paymentCardPayload);
        log.info("IYZICO_REQUEST_CREATED orderId={} username={} totalPrice={} itemCount={}",
                orderId, username, totalPrice, items.size());

        Payment payment = Payment.create(request, options());
        log.info("IYZICO_RESPONSE_RECEIVED orderId={} status={} errorCode={} errorMessage={}",
                orderId, payment.getStatus(), payment.getErrorCode(), payment.getErrorMessage());

        if ("success".equalsIgnoreCase(payment.getStatus())) {
            return new IyzicoPaymentResult(true, null);
        }

        String reason = StringUtils.hasText(payment.getErrorMessage())
                ? payment.getErrorMessage()
                : "Iyzico payment failed";
        return new IyzicoPaymentResult(false, reason);
    }

    private CreatePaymentRequest createRequest(Long orderId,
                                               String username,
                                               List<Map<String, Object>> items,
                                               BigDecimal totalPrice,
                                               Map<String, Object> paymentCardPayload) {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setLocale(Locale.TR.getValue());
        request.setConversationId(orderId.toString());
        request.setPrice(totalPrice);
        request.setPaidPrice(totalPrice);
        request.setCurrency(Currency.TRY.name());
        request.setInstallment(1);
        request.setBasketId("ORDER-" + orderId);
        request.setPaymentChannel(PaymentChannel.WEB.name());
        request.setPaymentGroup(PaymentGroup.PRODUCT.name());
        request.setPaymentCard(paymentCard(paymentCardPayload));
        request.setBuyer(buyer(username));

        Address address = address(username);
        request.setShippingAddress(address);
        request.setBillingAddress(address);
        request.setBasketItems(basketItems(items));
        return request;
    }

    private Options options() {
        Options options = new Options();
        options.setApiKey(apiKey);
        options.setSecretKey(secretKey);
        options.setBaseUrl(baseUrl);
        return options;
    }

    private PaymentCard paymentCard(Map<String, Object> paymentCardPayload) {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setCardHolderName(valueOrDefault(paymentCardPayload, "cardHolderName", cardHolderName));
        paymentCard.setCardNumber(valueOrDefault(paymentCardPayload, "cardNumber", cardNumber));
        paymentCard.setExpireMonth(valueOrDefault(paymentCardPayload, "expireMonth", expireMonth));
        paymentCard.setExpireYear(valueOrDefault(paymentCardPayload, "expireYear", expireYear));
        paymentCard.setCvc(valueOrDefault(paymentCardPayload, "cvc", cvc));
        paymentCard.setRegisterCard(0);
        return paymentCard;
    }

    private String valueOrDefault(Map<String, Object> payload, String key, String defaultValue) {
        if (payload == null || payload.get(key) == null || !StringUtils.hasText(payload.get(key).toString())) {
            return defaultValue;
        }

        return payload.get(key).toString();
    }

    private Buyer buyer(String username) {
        Buyer buyer = new Buyer();
        buyer.setId(username);
        buyer.setName(username);
        buyer.setSurname("Demo");
        buyer.setGsmNumber("+905350000000");
        buyer.setEmail(username + "@demo.com");
        buyer.setIdentityNumber("74300864791");
        buyer.setRegistrationAddress("Nidakule Goztepe, Istanbul");
        buyer.setIp("127.0.0.1");
        buyer.setCity("Istanbul");
        buyer.setCountry("Turkey");
        buyer.setZipCode("34732");
        return buyer;
    }

    private Address address(String username) {
        Address address = new Address();
        address.setContactName(username + " Demo");
        address.setCity("Istanbul");
        address.setCountry("Turkey");
        address.setAddress("Nidakule Goztepe, Istanbul");
        address.setZipCode("34732");
        return address;
    }

    private List<BasketItem> basketItems(List<Map<String, Object>> items) {
        List<BasketItem> basketItems = new ArrayList<>();

        for (Map<String, Object> item : items) {
            BasketItem basketItem = new BasketItem();
            basketItem.setId(item.get("productId").toString());
            basketItem.setName(item.get("productName").toString());
            basketItem.setCategory1("Product");
            basketItem.setItemType(BasketItemType.PHYSICAL.name());
            basketItem.setPrice(itemTotal(item));
            basketItems.add(basketItem);
        }

        return basketItems;
    }

    private BigDecimal calculateTotal(List<Map<String, Object>> items) {
        return items.stream()
                .map(this::itemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal itemTotal(Map<String, Object> item) {
        BigDecimal price = new BigDecimal(item.get("price").toString());
        BigDecimal quantity = new BigDecimal(item.get("quantity").toString());
        return price.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    }

    public record IyzicoPaymentResult(boolean success, String reason) {
    }
}
