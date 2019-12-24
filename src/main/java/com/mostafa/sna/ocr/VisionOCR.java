package com.mostafa.sna.ocr;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;

@RestController
public class VisionOCR {

	String nameBng, nameEng, father, husband, mother, dob, id;

	@RequestMapping(value = "/OCRFromSmartCardImage", method = RequestMethod.POST)
	public @ResponseBody HashMap<String, Object> updateKycImage(@RequestParam String api_pass,
			@RequestParam MultipartFile userimage) throws IOException {
		HashMap<String, Object> response = new LinkedHashMap<String, Object>();

		String UPLOADED_FOLDER = "/E:/images/";
		String text = "";

		if (!userimage.getOriginalFilename().isEmpty()) {
			BufferedOutputStream outputStream = new BufferedOutputStream(
					new FileOutputStream(new File(UPLOADED_FOLDER, "a" + ".jpg")));
			outputStream.write(userimage.getBytes());
			outputStream.flush();
			outputStream.close();
		}

		switch (api_pass.toLowerCase()) {

		case "updateimage":

			try {

				GoogleCredentials credentials = GoogleCredentials
						.fromStream(new FileInputStream("D:/Google Credintial.json"))
						.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
				Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

				Page<Bucket> buckets = storage.list();
				for (Bucket bucket : buckets.iterateAll()) {
					System.out.println(bucket.toString());

				}

				text = detectText(UPLOADED_FOLDER + "a.jpg");

				if (text.contains("National") || text.contains("Card") || text.contains("NID NO")) {

					snidTextProcess(text);

				} else if (text.contains("NATIONAL") || text.contains("CARD") || text.contains("ID NO")) {

					nidTextProcess(text);

				}

			} catch (Exception ex) {
				ex.printStackTrace();
				response.put("Response_Code", "1");
				response.put("Response_Status", "Technical Problem, Please Contact with Support Tream...");
				break;
			}
			response.put("Response_Code", "0");
			response.put("Response_Status", "Text Extracted Successfully...");

			response.put("Name Bangla", nameBng);
			response.put("Name English", nameEng);
			response.put("Father", father);
			response.put("Mother", mother);
			response.put("Husband", husband);
			response.put("DOB", dob);
			response.put("id", id);
			break;
		}
		return response;
	}

	public static String detectText(String filePath) throws Exception, IOException {
		String[] arr = null;

		List<AnnotateImageRequest> requests = new ArrayList<>();

		ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

		Image img = Image.newBuilder().setContent(imgBytes).build();
		Feature feat = Feature.newBuilder().setType(Type.DOCUMENT_TEXT_DETECTION).build();
		AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
		requests.add(request);

		try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
			BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
			List<AnnotateImageResponse> responses = response.getResponsesList();

			for (AnnotateImageResponse res : responses) {
				if (res.hasError()) {
					System.out.println("Error: " + res.getError().getMessage());
				}

				arr = new String[res.getTextAnnotationsList().size()];
				int i = 0;

				// For full list of available annotations, see http://g.co/cloud/vision/docs
				for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
					arr[i] = annotation.getDescription();
					i++;
//					System.out.println("Text: " + annotation.getDescription());
				}

			}
		}

		String text = arr[0];
		System.out.println("Text: " + text);
		return text;

	}

	public void snidTextProcess(String text) throws ParseException {

		SNIDTextProcess snid = new SNIDTextProcess();

		String[] pText = snid.processText(text);

		nameBng = pText[0];
		nameEng = pText[1];
		father = pText[2];
		husband = pText[3];
		mother = pText[4];
		dob = pText[5];
		id = pText[6];
		
		dob = dateFormat(dob);
		
	}

	public void nidTextProcess(String text) throws ParseException {

		NIDTextProcess nid = new NIDTextProcess();

		String[] pText = nid.processText(text);

		nameBng = pText[0];
		nameEng = pText[1];
		father = pText[2];
		husband = pText[3];
		mother = pText[4];
		dob = pText[5];
		id = pText[6];

		dob = dateFormat(dob);
		
	}
	
	public String dateFormat(String date) throws ParseException {
		SimpleDateFormat parser = new SimpleDateFormat("dd MMM yyyy");
		Date parsedDate = parser.parse(date);
		
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
		String formatedDate = formater.format(parsedDate);

		return formatedDate;
	}
}
