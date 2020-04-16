package com.mostafa.sna.ocr;

import java.io.*;
import java.text.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;

@RestController
public class VisionOCR {

	String nameBng, nameEng, father, husband, mother, dob, id;

	String UPLOADED_FOLDER = "E:\\images\\OCR Saved Image\\";

	@RequestMapping(value = "/OCR", method = RequestMethod.POST)
	public @ResponseBody HashMap<String, Object> processImage(@RequestParam MultipartFile userimage)
			throws IOException {

		HashMap<String, Object> response = new LinkedHashMap<String, Object>();

		String text = "";

		if (!userimage.getOriginalFilename().isEmpty()) {
			BufferedOutputStream outputStream = new BufferedOutputStream(
					new FileOutputStream(new File(UPLOADED_FOLDER, "a" + ".jpg")));
			outputStream.write(userimage.getBytes());
			outputStream.flush();
			outputStream.close();
		}

		try {

			GoogleCredentials credentials = GoogleCredentials
					.fromStream(new FileInputStream("E:\\Resource\\Google Credintial\\myproject6f515bc33cdf.json"))
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

			response.put("Response_Code", "0");
			response.put("Response_Status", "Text Extracted Successfully...");

			response.put("Name Bangla", nameBng);
			response.put("Name English", nameEng);
			response.put("Father", father);
			response.put("Mother", mother);
			response.put("Husband", husband);
			response.put("DOB", dob);
			response.put("id", id);

		} catch (Exception ex) {
			ex.printStackTrace();
			response.put("Response_Code", "1");
			response.put("Response_Status", "Technical Problem, Please Contact with Support Tream...");
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

				for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
					arr[i] = annotation.getDescription();
					i++;
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
