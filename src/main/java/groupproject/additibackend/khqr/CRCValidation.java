package groupproject.additibackend.khqr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CRCValidation {
    private boolean valid;
    private String expectedCRC;
    private String actualCRC;
}
