package cn.ismartv.truetime;

import java.io.IOException;

public class InvalidNtpServerResponseException
      extends IOException {
    InvalidNtpServerResponseException(String detailMessage) {
        super(detailMessage);
    }
}
