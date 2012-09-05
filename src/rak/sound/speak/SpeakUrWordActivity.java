package rak.sound.speak;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;



public class SpeakUrWordActivity extends Activity implements OnClickListener, OnInitListener
{
	
	private static final int VR_REQ = 999;
	private ListView wordList;
	private final String LOG_TAG = "SpeakUrWordActivity";
	private int DATA_CHECK_CODE=0;
	
	private TextToSpeech repeatTTS;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button speechBtn = (Button) findViewById(R.id.speech_button);
        wordList =(ListView) findViewById(R.id.word_list);
        PackageManager pkMg = getPackageManager();
        List<ResolveInfo> intActivities= pkMg.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        
        if(intActivities.size()!=0)
        {
        	speechBtn.setOnClickListener(this);
        	Intent CheckTTSIntent = new Intent();
        	// Check TTS Data
        	CheckTTSIntent.setAction( TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        	startActivityForResult ( CheckTTSIntent , DATA_CHECK_CODE);
        }
        else
        {
        	speechBtn.setEnabled(false);
        	Toast.makeText(this, "Ooops - Speech Recognition NOT supported!! Your Device Probably too OLD, Get a New One.", Toast.LENGTH_LONG).show();
        }
        wordList.setOnItemClickListener( new OnItemClickListener()
        	{
        		public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
        		{
        			// Casting
        			TextView wordView = (TextView) view;
        			String wordChosen= (String) wordView.getText();
        			// For Debugging
        			Log.v(LOG_TAG, "chosen: "+wordChosen);
        			Toast.makeText( SpeakUrWordActivity.this, "You Said: "+wordChosen, Toast.LENGTH_LONG).show();
        			repeatTTS.speak("You Said: "+wordChosen, TextToSpeech.QUEUE_FLUSH, null);
        		}
        	
        	});
    }
    
	public void onInit(int initStatus) 
	{
		if(initStatus == TextToSpeech.SUCCESS)
		{
			repeatTTS.setLanguage( Locale.US);
		}
		
		
	}

	public void onClick(View v)
	{
		if(v.getId() == R.id.speech_button)
		{
			listenToSpeech();
		}	
		
	}
	private void listenToSpeech() 
	{		
		Intent listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		
		listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
		listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please Say Something!!");
		
		listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		
		listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
		
		startActivityForResult(listenIntent, VR_REQ);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == VR_REQ && resultCode == RESULT_OK)
		{
			ArrayList<String> suggestedWords = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			wordList.setAdapter(new ArrayAdapter<String> (this,R.layout.word, suggestedWords));
		}
		if(requestCode == DATA_CHECK_CODE )
		{
			// assuming we have data
			if(requestCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
				repeatTTS = new TextToSpeech(this,this);
			// Data not Installed, Prompt user.
			else
			{
				Intent installTTS = new Intent();
				installTTS.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installTTS);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}