package com.serenitystay.hotel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "slip_reference", length = 50)
    private String slipReference;

    @Column(name = "bank_account", length = 50)
    private String bankAccount;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;


    // =========================================================
    // PAYMENT METHOD
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod = PaymentMethod.CASH;


    // =========================================================
    // CARD PAYMENT DETAILS
    // =========================================================

    /*
     * IMPORTANT:
     * We only store basic card information.
     *
     * We DO NOT store:
     * - Full card number
     * - CVV
     * - PIN
     */

    @Column(name = "cardholder_name", length = 100)
    private String cardholderName;

    @Column(name = "card_type", length = 30)
    private String cardType;

    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;


    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public Payment() {
    }

    public Payment(
            Reservation reservation,
            BigDecimal amount,
            String bankAccount,
            String slipReference,
            LocalDateTime transactionDate) {

        this.reservation = reservation;
        this.amount = amount;
        this.bankAccount = bankAccount;
        this.slipReference = slipReference;
        this.transactionDate = transactionDate;
        this.paymentDate = LocalDateTime.now();
    }

    /*
     * Simplified constructor for creating a payment
     * with reservation, amount and slip reference.
     */
    public Payment(
            Reservation reservation,
            BigDecimal amount,
            String slipReference) {

        this.reservation = reservation;
        this.amount = amount;
        this.slipReference = slipReference;
        this.paymentDate = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
    }


    // =========================================================
    // GETTERS AND SETTERS
    // =========================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getSlipReference() {
        return slipReference;
    }

    public void setSlipReference(String slipReference) {
        this.slipReference = slipReference;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDateTime getRefundDate() {
        return refundDate;
    }

    public void setRefundDate(LocalDateTime refundDate) {
        this.refundDate = refundDate;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }


    // =========================================================
    // PAYMENT METHOD GETTER / SETTER
    // =========================================================

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }


    // =========================================================
    // CARD DETAILS GETTERS / SETTERS
    // =========================================================

    public String getCardholderName() {
        return cardholderName;
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }


    // =========================================================
    // ENUMS
    // =========================================================

    public enum PaymentStatus {
        PENDING,
        VERIFIED,
        REFUNDED,
        FAILED
    }

    public enum PaymentMethod {
        CASH,
        CARD,
        BANK_TRANSFER
    }
}