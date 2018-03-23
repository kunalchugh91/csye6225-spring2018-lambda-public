package mylambdas;


import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;

public class ResetPasswordLambda implements RequestHandler<SNSEvent, Object> {


    public Object handleRequest(SNSEvent snsEvent, Context context) {
        return null;
    }
}
