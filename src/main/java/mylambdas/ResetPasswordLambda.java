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



import java.io.IOException;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

import java.util.Date;
import java.util.Iterator;

public class ResetPasswordLambda implements RequestHandler<SNSEvent, Object>
{


    public Object handleRequest(SNSEvent snsEvent, Context context)
    {


        String FROM = "";
        String SUBJECT = "Email Reset Request";
        String TEXTBODY = "This email was sent through Amazon SES ";
        String userid = "";

        long currenttime = new Date().getTime();


        // get the userid passed in the snsEvent
        Iterator<SNSEvent.SNSRecord> it = snsEvent.getRecords().iterator();
        if (it.hasNext())
        {
            SNSEvent.SNSRecord record = it.next();
            SNSEvent.SNS sns = record.getSNS();
            userid = sns.getMessageAttributes().get("to_email").getValue();
            FROM = sns.getMessageAttributes().get("from_email").getValue();
        }


        // set up dynamoDB client
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable("csye6225");

        // number of seconds elapsed since 12:00:00 AM January 1st, 1970 UTC.


        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("userid = :v_uid")
                .withFilterExpression("expirationtime > :v_currenttime")
                .withValueMap(new ValueMap()
                        .withString(":v_uid", userid)
                        .withString(":v_currenttime", Long.toString(currenttime)));


        ItemCollection<QueryOutcome> items = table.query(spec);

        Iterator<Item> iterator = items.iterator();

        // if token does not already exist
        if (!iterator.hasNext())
        {
            // insert item into dynamo db
            Item item = new Item()
                    .withPrimaryKey("userid", userid)
                    .withPrimaryKey("expirationtime", Long.toString(new Date().getTime()));

            // put the item into table
            PutItemOutcome outcome = table.putItem(item);
            

            // send email to user

            try
            {
                AmazonSimpleEmailService client_email =
                        AmazonSimpleEmailServiceClientBuilder.standard()
                                // Replace US_WEST_2 with the AWS Region you're using for
                                // Amazon SES.
                                .withRegion(Regions.US_EAST_1).build();
                SendEmailRequest request = new SendEmailRequest()
                        .withDestination(
                                new Destination().withToAddresses(userid))
                        .withMessage(new Message()
                                .withBody(new Body()
                                        .withText(new Content()
                                                .withCharset("UTF-8").withData(TEXTBODY)))
                                .withSubject(new Content()
                                        .withCharset("UTF-8").withData(SUBJECT)))
                        .withSource(FROM);


                client_email.sendEmail(request);

            }
            catch (Exception ex)
            {
                System.out.println("The email was not sent. Error message: "+ ex.getMessage());
            }


        }


        return null;
    }
}