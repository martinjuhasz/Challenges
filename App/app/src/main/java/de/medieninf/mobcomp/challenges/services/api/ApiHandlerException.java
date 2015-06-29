package de.medieninf.mobcomp.challenges.services.api;

public class ApiHandlerException extends Exception{
    private ApiHandler.ErrorCode errorCode;

    public ApiHandlerException(ApiHandler.ErrorCode errorCode){
        this.errorCode = errorCode;
    }

    public ApiHandler.ErrorCode getErrorCode() {
        return errorCode;
    }
}
