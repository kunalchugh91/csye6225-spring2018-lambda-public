package mylambdas;


import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;

import java.util.Date;
import java.util.Iterator;

public class ResetPasswordLambda implements RequestHandler<SNSEvent, Object> {


    public Object handleRequest(SNSEvent snsEvent, Context context) {

        // get the userid passed in the snsEvent
        String userid = "";

        // set up dynamoDB client
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable("csye6225");

        // number of seconds elapsed since 12:00:00 AM January 1st, 1970 UTC.
        long currenttime = new Date().getTime();

        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("userid = :v_uid")
                .withFilterExpression("expirationtime > :v_currenttime")
                .withValueMap(new ValueMap()
                        .withString(":v_uid", userid)
                        .withString(":v_currenttime", Long.toString(currenttime)));


        ItemCollection<QueryOutcome> items = table.query(spec);

        Iterator<Item> iterator = items.iterator();

        // if token does not already exist
        if (!iterator.hasNext()) {
            // insert item into dynamo db
            Item item = new Item()
                    .withPrimaryKey("userid", userid)
                    .withPrimaryKey("expirationtime", Long.toString(new Date().getTime()));

            // put the item into table
            PutItemOutcome outcome = table.putItem(item);
            

            // send email to user

        }


        return null;
    }
}
