/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.amazon.customskill;

import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;   
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;





/*
 * This class is the actual skill. Here you receive the input and have to produce the speech output. 
 */
public class AlexaSkillSpeechlet implements SpeechletV2{
	
	static Logger logger = LoggerFactory.getLogger(AlexaSkillSpeechlet.class);
	
	public static String userRequest;

			
	private static String confirmEnglish = "Ab jetzt werde ich alle Wörter von englisch   auf deutsch übersetzen bis du fertig sagst.  viel spass beim lesen!";
	private static String confirmDeutsch = "Ab jetzt werde ich alle Wörter von deutsch   auf englisch übersetzen bis du fertig sagst.  viel spass beim lesen!";

	private static String question;
	private static String frage;

	private static String correctAnswer;
	
	
	private static enum RecognitionState {Words,Wort, EnglishDeutsch,Auswahl, YesNo,JaNein, Quiz,QuizD};
	private RecognitionState recState;
	private static enum UserIntent {Englisch, Deutsch, Yes, No,Ja,Nein, Error, Fertig};
	UserIntent ourUserIntent;

	static String welcomeMsg = "Hallo, in welcher Sprache wollen Sie das Buch lesen? englisch oder deutsch?.";
	static String wrongMsg = "Das ist leider falsch.";
	static String correctMsg = "Das ist richtig.";
	static String continueQuiz = "Ich habe für dich ein Vokabel Quiz vorbereitet. Möchtest du das spielen?";
	
	static String goodbyeMsg = "Danke und bis zum nächsten Mal.";
	static String Allwords="DU hast Alle wörter geübt,Danke und bis zum nächsten Mal. ";
	static String errorEnglishDeutschMsg = "Das habe ich nicht verstanden. Sagen Sie bitte Englisch oder Deutsch.";
	static String errorYesNoMsg = "Das habe ich nicht verstanden. Sagen Sie bitte JA oder Nein.";
	
	public static int count = 1;
	public static int topEng=1;
	public static int topDe=1;

	@Override
	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope)
	{
		logger.info("Alexa session begins");
		
		recState = RecognitionState.EnglishDeutsch;
	
	}
	
	public int getCount() {
		return count;
	}

	@Override
	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope)
	{
		/*
		 * try { selectQuestion(); Fragen(); } catch (Exception e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
		return askUserResponse(welcomeMsg);
	}
	
	
	//Englisch Wörter von der Datenbank übersetzen
	private void selectQuestion() throws Exception{
		String fromLang ="en";
		String toLang="de";
		Connection con = DBSqlite.createConnection();
		String daten = DBSqlite.selectEnglisch(con, count);
		String answer = (Translator.translate(fromLang, toLang, daten)).toLowerCase();
		replaceUmlaute(answer);
		String answerOhne = (replaceUmlaute(answer)).toLowerCase();
		
		question="was bedeutet "+ daten; correctAnswer = (answerOhne);
		count++;
	}
	
	
	//Deutsche Wörter von der Datenbank übersetzen
	private void Fragen() throws Exception{
		String fromLang ="de";
		String toLang="en";
		Connection con = DBSqlite.createConnection();
		String daten = DBSqlite.selectDeutsch(con, count);
	
		
		frage="was bedeutet "+ daten; correctAnswer = (Translator.translate(fromLang, toLang, DBSqlite.selectDeutsch(con, count).toLowerCase()).toLowerCase());
		count++;
	}


	@Override
	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope)
	{
		IntentRequest request = requestEnvelope.getRequest();
		Intent intent = request.getIntent();
		
		userRequest = intent.getSlot("anything").getValue();
		Connection con = null;
		
		
		logger.info("Received following text: [" + userRequest + "]");
		logger.info("recState is [" + recState + "]");
		SpeechletResponse resp = null;
		switch (recState) {
		case EnglishDeutsch: resp = evaluateEnglishDeutsch(userRequest); break;
		case Words: try {
			
						
						resp = evaluateWords(userRequest,con);
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}; break;
		case Wort: try {
			
			
			resp = evaluateWort(userRequest,con);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}; break;
		case YesNo: resp = evaluateYesNo(userRequest); break;
		case JaNein: resp = evaluateJaNein(userRequest);break;
		case Quiz: resp = evaluateQuiz(userRequest); break;
		case QuizD: resp = evaluateQuizD(userRequest); break;
		
		default: resp = response("Erkannter Text: " + userRequest);
		}   
		return resp;
	}

	private SpeechletResponse evaluateEnglishDeutsch(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
			case Englisch: {
				res = askUserResponse(confirmEnglish); recState = RecognitionState.Words;break;
				
			} case Deutsch: {
				res = askUserResponse(confirmDeutsch);recState = RecognitionState.Wort; break;
			} default: {
				res = askUserResponse(errorEnglishDeutschMsg);
			};break;
		}
		return res;
	}
	
	//Umlaut umwandeln
	private static String[][] UMLAUT_REPLACEMENTS = { { "Ä", "Ae" }, { "Ü", "Ue" }, { "Ö", "Oe" }, { "ä", "ae" }, { "ü", "ue" }, { "ö", "oe" }, { "ß", "ss" } };
	public static String replaceUmlaute(String orig) {
	    String result = orig;

	    for (int i = 0; i < UMLAUT_REPLACEMENTS.length; i++) {
	        result = result.replaceAll(UMLAUT_REPLACEMENTS[i][0], UMLAUT_REPLACEMENTS[i][1]);
	    }

	    return result;
	}


	//CASE ENGLISH
	private SpeechletResponse evaluateWords(String userRequest,Connection con) throws Exception {
		String toLang="de";
		String fromLang="en";


		SpeechletResponse res = null;
		con = DBSqlite.createConnection();
		DBSqlite.createTableEnglisch(con);
		String a = userRequest.toLowerCase();
		
		if(!a.equals("fertig")){
			
			
			String answer = Translator.translate(fromLang, toLang, a);
			replaceUmlaute(answer);
			String loesung = (replaceUmlaute(answer)).toLowerCase();
			topEng++;
			DBSqlite.insertEnglisch(con,a );		
			res = askUserResponse(a +" bedeutet : "+loesung);
			
			
		}else {
			res = askUserResponse(continueQuiz); recState= RecognitionState.YesNo;
		}
		

		return res;
		
	}
	
	
	//CASE DEUTSCH 
	private SpeechletResponse evaluateWort(String userRequest,Connection con) throws Exception {
		String toLang="en";
		String fromLang="de";
		SpeechletResponse res = null;
		con = DBSqlite.createConnection();
		DBSqlite.createTableDeutsch(con);
		String b = userRequest.toLowerCase();
		String OhneUmlauts = replaceUmlaute(b);
		if(!OhneUmlauts.equals("fertig")){
			
				
				String answers = Translator.translate(fromLang, toLang, OhneUmlauts);
				
			topDe++;
			DBSqlite.insertDeutsch(con,OhneUmlauts );		
			res = askUserResponse(OhneUmlauts+" means : "+ answers);
			
			
		}else {
			res = askUserResponse(continueQuiz); recState= RecognitionState.JaNein;
		}
		

		return res;
		
	}
	
	//Quiz Case English
	private SpeechletResponse evaluateYesNo(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
			case Yes: {
				if(count!=topEng) {
					try {
						selectQuestion();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					res = askUserResponse(question);
					
					recState = RecognitionState.Quiz;
				}else {
					res=askUserResponse(Allwords);
				}
			}; break;
			case No: {
				res = askUserResponse(goodbyeMsg);
			}; break;	
			default: {
				res = askUserResponse(errorYesNoMsg);
			}
		}
			return res;
	}
	
	
	//Quiz Case Deutsch
		private SpeechletResponse evaluateJaNein(String userRequest) {
			SpeechletResponse res = null;
			recognizeUserIntent(userRequest);
			switch (ourUserIntent) {
			
				case Yes: {
					if(count!=topDe) {
					try {
						Fragen();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					res = askUserResponse(frage);
					
					recState = RecognitionState.QuizD;
				}else {
					res=askUserResponse(Allwords);
				}
				}; break;
				case No: {
					res = askUserResponse(goodbyeMsg);
				}; break;	
				default: {
					res = askUserResponse(errorYesNoMsg);
				}
			}
				return res;
		}
		
		
		//Antwort prüfen Case English
	private SpeechletResponse evaluateQuiz(String userRequest) {
		SpeechletResponse res = null;
		
		if (userRequest.toLowerCase().equals(correctAnswer)) {
			
			res= askUserResponse("Richtig!. Weiter?");
			
			recState = RecognitionState.YesNo;
			
		}else {
			
			res= askUserResponse("richtige Antwort lautet: "+correctAnswer+"!. Weiter?");
			recState = RecognitionState.YesNo;
			
		}
		return res;
	}
	
	//Antwort prüfen Case Deutsch
	private SpeechletResponse evaluateQuizD(String userRequest) {
		SpeechletResponse res = null;
		
		if (userRequest.toLowerCase().equals(correctAnswer)) {
			
			res= askUserResponse("Richtig!. Weiter?");
			
			recState = RecognitionState.JaNein;
			
		}else {
			
			res= askUserResponse("richtige Antwort lautet: "+correctAnswer+"!. Weiter?");
			recState = RecognitionState.JaNein;
			
		}
		return res;
	}
	
	
	


	//TODO
	/*
	 * private void recognizeUserIntent(String userRequest) { switch
	 * (userRequest.toLowerCase()) { case "englisch": ourUserIntent =
	 * UserIntent.Englisch; break; case "deutsch": ourUserIntent =
	 * UserIntent.Deutsch; break;
	 * 
	 * case "ja": ourUserIntent = UserIntent.Yes; break; case "nein": ourUserIntent
	 * = UserIntent.No; break;
	 * 
	 * 
	 * 
	 * } logger.info("set ourUserIntent to " +ourUserIntent); }
	 */
	
	public void recognizeUserIntent(String userRequest) {
		userRequest = userRequest.toLowerCase();
		String pattern1 = "(ich möchte )?(ich will )?(ich nehme )?(auf)?deutsch( bitte)?";
		String pattern2 = "(ich möchte )?(ich will )?(ich nehme )?(auf)?englisch( bitte)?";
	
		String pattern3 = "\\bnein\\b";
		String pattern4 = "\\bja\\b";

		Pattern p1 = Pattern.compile(pattern1);
		Matcher m1 = p1.matcher(userRequest);
		Pattern p2 = Pattern.compile(pattern2);
		Matcher m2 = p2.matcher(userRequest);
		Pattern p3 = Pattern.compile(pattern3);
		Matcher m3 = p3.matcher(userRequest);
		Pattern p4 = Pattern.compile(pattern4);
		Matcher m4 = p4.matcher(userRequest);
		
		if (m1.find()) {
			ourUserIntent = UserIntent.Deutsch;

		} else if (m2.find()) {
			ourUserIntent = UserIntent.Englisch;
		} else if (m3.find()) {
			ourUserIntent = UserIntent.No;
		} else if (m4.find()) {
			ourUserIntent = UserIntent.Yes;
		} else {
			ourUserIntent = UserIntent.Error;
		}
		logger.info("set ourUserIntent to " +ourUserIntent);
	}

	/**
	 * formats the text in weird ways
	 * @param text
	 * @param i
	 * @return
	 */
	

	@Override
	public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope)
	{
		logger.info("Alexa session ends now");
	}



	/**
	 * Tell the user something - the Alexa session ends after a 'tell'
	 */
	private SpeechletResponse response(String text)
	{
		// Create the plain text output.
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(text);

		return SpeechletResponse.newTellResponse(speech);
	}

	/**
	 * A response to the original input - the session stays alive after an ask request was send.
	 *  have a look on https://developer.amazon.com/de/docs/custom-skills/speech-synthesis-markup-language-ssml-reference.html
	 * @param text
	 * @return
	 */
	private SpeechletResponse askUserResponse(String text)
	{
		
		SsmlOutputSpeech speech = new SsmlOutputSpeech();
		speech.setSsml("<speak>" + text + "</speak>");

		// reprompt after 8 seconds
		SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
		repromptSpeech.setSsml("<speak><emphasis level=\"strong\">Hey!</emphasis> Bist du noch da?</speak>");

		Reprompt rep = new Reprompt();
		rep.setOutputSpeech(repromptSpeech);

		return SpeechletResponse.newAskResponse(speech, rep);
	}

	String pattern1 = "(ich möchte )?(ich will )?(ich nehme )?(auf)?deutsch( bitte)?";


}
