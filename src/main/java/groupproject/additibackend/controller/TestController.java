package groupproject.additibackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/dashboard")
    public ResponseEntity<String> home() {
        return new ResponseEntity<>("This can be seen after login", HttpStatus.OK);
    }
}
