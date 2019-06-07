package com.chef.assist.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CaResponse {
    private Object data;
    private Boolean ok;
    private String message;

    public static CaResponse makeResponse(boolean o, String m, Object d){
        CaResponse r = new CaResponse();
        r.setData(d);
        r.setOk(o);
        r.setMessage(m);
        return r;
    }
}
