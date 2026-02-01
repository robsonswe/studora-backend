package com.studora.config;

import com.studora.util.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;

@Configuration
@ControllerAdvice
public class WebConfig {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null) {
                    setValue(null);
                } else {
                    setValue(StringUtils.normalizeSpace(text));
                }
            }
        });
    }
}
