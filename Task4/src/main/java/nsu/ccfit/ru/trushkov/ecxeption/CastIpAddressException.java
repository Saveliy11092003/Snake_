package nsu.ccfit.ru.trushkov.ecxeption;

public class CastIpAddressException extends RuntimeException {
    public CastIpAddressException(String ip) {
        super("invalid ip address: " + ip);
    }
}
