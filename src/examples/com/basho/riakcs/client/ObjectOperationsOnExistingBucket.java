package examples.com.basho.riakcs.client;

import java.io.*;
import java.util.*;

import org.json.*;

import com.basho.riakcs.client.api.*;


public class ObjectOperationsOnExistingBucket
{
	public static void runIt(boolean runAgainstRiakCS, boolean enableDebugOutput) throws Exception
	{
		RiakCSClient csClient= null;

		String bucketName    = "playground-123";      // bucket has to exist, and has to be accessible by user
		String objectKey     = "playground/testfile";
		String outputFilename= null;

		if (runAgainstRiakCS)
		{
			CSCredentials csCredentials= new CSCredentials(CSCredentials.class.getResourceAsStream("CSCredentials.Riak.properties"));			
			csClient= new RiakCSClient(csCredentials.getCSAccessKey(), csCredentials.getsCSSecretKey(), csCredentials.getCSEndPoint(), false);

			outputFilename= "/tmp/riakout.txt";

		} else {
			CSCredentials s3Credentials= new CSCredentials(CSCredentials.class.getResourceAsStream("CSCredentials.AWS.properties"));
			csClient= new RiakCSClient(s3Credentials.getCSAccessKey(), s3Credentials.getsCSSecretKey());

			outputFilename= "/tmp/awsout.txt";
			
		}

		if (enableDebugOutput) csClient.enableDebugOutput();

		runItImpl(csClient, bucketName, objectKey, outputFilename);
	}

	private static void runItImpl(RiakCSClient csClient, String bucketName, String objectKey, String outputFilename) throws Exception
	{
		JSONObject result= null;


		// upload object
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "text/html");

		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put("Description","this is just a description");
		
		String webpage= "<html><body>This is a <b>Web Page</b></body></html>";
		InputStream dataInputStream= new ByteArrayInputStream(webpage.getBytes("UTF-8"));

		csClient.createObject(bucketName, objectKey, dataInputStream, headers, metadata);


		// get object info
		result= csClient.getObjectInfo(bucketName, objectKey);
		System.out.println(result.toString(2));

		// get ACL
		result= csClient.getACLForObject(bucketName, objectKey);
		System.out.println(result.toString(2));

		// set ACL
		if (csClient.endpointIsS3() == false)
		{
			// add additional ACL, user has to exist
			csClient.addAdditionalACLToObject(bucketName, objectKey, "hugo@test.com", RiakCSClient.Permission.READ);

			// get ACL
			result= csClient.getACLForObject(bucketName, objectKey);
			System.out.println(result.toString(2));
		}

		// set "canned" ACL
		if (csClient.endpointIsS3()) // there is currently an issue with CS
		{
			csClient.setCannedACLForObject(bucketName, objectKey, RiakCSClient.PERM_PUBLIC_READ);
	
			// get ACL
			result= csClient.getACLForObject(bucketName, objectKey);
			System.out.println(result.toString(2));
		}

		// get object, content comes as part of the JSONObject
		result= csClient.getObject(bucketName, objectKey);
		System.out.println(result.toString(2));

		// get object, write content to file
		result= csClient.getObject(bucketName, objectKey, new FileOutputStream(outputFilename));
		System.out.println(result.toString(2));

		// list objects
		result= csClient.listObjects(bucketName);		
		System.out.println(result.toString(2));
		
		// delete object
		csClient.deleteObject(bucketName, objectKey);
		
		// list objects
		result= csClient.listObjects(bucketName);		
		System.out.println(result.toString(2));
	
		
		// try out some larger file
//		File uploadFile= new File("/tmp/riak-0.14.0-osx-i386.tar");
//		csClient.createObject(bucketName, "someLargeFile", new FileInputStream(uploadFile), null, null);
//		result= csClient.getObject(bucketName, "someLargeFile", new FileOutputStream("/tmp/someLargeFile.tar"));
//		System.out.println(result.toString(2));
//		csClient.deleteObject(bucketName, "someLargeFile");
		
	}

}