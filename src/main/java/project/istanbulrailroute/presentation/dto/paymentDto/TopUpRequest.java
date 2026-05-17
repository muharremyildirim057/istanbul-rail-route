package project.istanbulrailroute.presentation.dto.paymentDto;

public class TopUpRequest {
    private String creditCardNumber;
    private double amount;

    // Getter ve Setter Metotları
    public String getCreditCardNumber() { return creditCardNumber; }
    public void setCreditCardNumber(String creditCardNumber) { this.creditCardNumber = creditCardNumber; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
