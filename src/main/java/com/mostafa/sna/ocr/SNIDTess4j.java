package com.mostafa.sna.ocr;

public class SNIDTess4j {
	
	String [] splTxt = new String [7];
	
	public String [] processText(String text) {
		splTxt[0] = spliteText(text, "নাম");
		splTxt[1] = spliteText(text, "Name");
		if (text.contains("পিতা")) {
			splTxt[2] = spliteText(text, "পিতা");
		} else if (text.contains("স্বামী")) {
			splTxt[3] = spliteText(text, "স্বামী");
		}
		splTxt[4] = spliteText(text, "মাতা");
		splTxt[5] = spliteDate(text, "Birth");
		splTxt[6] = spliteNID(text, "NID No.");
		return splTxt;
	}

	public String spliteText(String text, String key) {
		if (text.contains(key)) {
			String[] splitArr = text.split(key);
			String splitArr2[] = splitArr[1].split("\\r?\\n");
			String splitArr3[] = splitArr2[1].split("\\r?\\n");
			String splitArr4[] = splitArr3[0].split("\\r?\\n");
			String splitValue = splitArr4[0].trim();
			if (key.contains("Name")) {
				splitValue = engTextClean(splitValue);
			} else {
				splitValue = bngTextClean(splitValue);
			}
			
			System.out.println(key+" "+ splitValue);
			
			return splitValue;
			
		} else {
			System.out.println('"'+key+'"'+" keyword not found");
			return "";
		}
	}
	
	public String spliteDate(String text, String key) {
		if (text.contains(key)) {
			String[] splitArr = text.split(key);
			String splitArr2[] = splitArr[1].split("\\r?\\n");
			String splitValue = splitArr2[0].trim();
			
			splitValue = splitValue.replaceAll( "[^a-zA-Z0-9./ -]" , "" );
			splitValue = splitValue.trim();
			if((splitValue.charAt(splitValue.length()-1)=='i'|| splitValue.charAt(splitValue.length()-1)=='1') && splitValue.charAt(splitValue.length()-2)==' ') {
				splitValue = splitValue.substring(0, splitValue.length() - 2);
			}
			splitValue = splitValue.trim();
			
			System.out.println(key+" "+ splitValue);
			
			return splitValue;
			
		} else {
			System.out.println('"'+key+'"'+" keyword not found");
			return "";
		}
	}
	
	public String spliteNID(String text, String key) {
		if (text.contains(key)) {
			String[] splitArr = text.split(key);
			String splitArr2[] = splitArr[1].split("\\r?\\n");
			String splitValue = splitArr2[0].trim();
			
			splitValue = splitValue.replaceAll( "[^0-9]" , "" );
			splitValue = splitValue.trim();
			
			System.out.println(key+" "+ splitValue);
			
			return splitValue;
			
		} else {
			System.out.println('"'+key+'"'+" keyword not found");
			return "";
		}
	}
	
	public String numberClean(String num) {
		String number = num.replaceAll("[^\\d]", "" );
		number = number.trim();
		return number;
	}
	
	public String engTextClean(String engText) {
		String txt = engText.replaceAll( "[^a-zA-Z. ]" , "" );
		txt = txt.trim();
		if((txt.charAt(txt.length()-1)=='i'|| txt.charAt(txt.length()-1)=='1') && txt.charAt(txt.length()-2)==' ') {
			txt = txt.substring(0, txt.length() - 2);
		}
		txt = txt.trim();
		return txt;
	}
	
	public String bngTextClean(String bngText) {
		String txt = bngText.replaceAll( "[^\\pL\\pM() ]+" , "" );
		txt = txt.replaceAll( "[a-zA-Z0-9]" , "" );
		txt = txt.trim();
		return txt;
	}
	
}
