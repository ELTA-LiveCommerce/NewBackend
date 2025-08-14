package kr.elta.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ResponseDTO<T> {
    private boolean result;
    private String message;
    private T data;

    public ResponseDTO(boolean result){
        this.result = result;
        this.message = null;
        this.data = null;
    }

    public ResponseDTO(boolean result, String message) {
        this.result = result;
        this.message = message;
        this.data = null;
    }

//    public ResponseDTO(boolean result, Object data){
//        this.result = result;
//        this.message = null;
//        this.data = data;
//    }
}