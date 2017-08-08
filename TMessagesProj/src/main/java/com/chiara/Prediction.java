package com.chiara;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.telegram.messenger.ApplicationLoader;

import java.io.IOException;
import java.io.InputStream;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

/**
 * Created by Chiara on 19/04/17.
 */

public class Prediction {

    String Mcontext;
    int MnumberOfBackspace;
    int McommentLength;
    double MbackspaceFrequency;
    int MerasedTextLength;
    String MpartOfTheDay;
    double MtypingSpeed;
    int MtouchCount;
    String Mweather;
    String predizione = null;

    public Prediction(String context, int numberOfBackspace, int commentLength, double backspaceFrequency, int erasedTextLength, String partOfTheDay, double typingSpeed, int touchCount, String weather){
        Mcontext = context;
        MnumberOfBackspace = numberOfBackspace;
        McommentLength = commentLength;
        MbackspaceFrequency = backspaceFrequency;
        MerasedTextLength = erasedTextLength;
        MpartOfTheDay = partOfTheDay;
        MtypingSpeed = typingSpeed;
        MtouchCount = touchCount;
        Mweather = weather;
    }

    public String doPrediction(){
        try{
            int NUMBER_OF_ATTRIBUTES = 8; //7 + 1 class
            int NUMBER_OF_INSTANCES = 1;

            FastVector contextVector = new FastVector(4);
            contextVector.add("Home");
            contextVector.add("Work");
            contextVector.add("Entertainment");
            contextVector.add("Commute");

            FastVector weatherVector = new FastVector(8);
            weatherVector.add("Fog");
            weatherVector.add("Snow");
            weatherVector.add("Clouds");
            weatherVector.add("Clear");
            weatherVector.add("Drizzle");
            weatherVector.add("Mist");
            weatherVector.add("Rain");
            weatherVector.add("Thunderstorm");

            FastVector emotionVector = new FastVector(3);
            emotionVector.add("positive");
            emotionVector.add("neutral");
            emotionVector.add("negative");

            FastVector timeVector = new FastVector(4);
            timeVector.add("morning");
            timeVector.add("afternoon");
            timeVector.add("evening");
            timeVector.add("night");

            Attribute context = new Attribute("context", contextVector);
            Attribute weather = new Attribute("weather", weatherVector);
            Attribute backspace_frequency = new Attribute("backspace_frequency");
            Attribute erased_length = new Attribute("erased_length");
            Attribute time = new Attribute("time",timeVector);
            Attribute typing_speed = new Attribute("typing_speed");
            Attribute touch_count = new Attribute("touch_count");
            Attribute emotion = new Attribute("emotion", emotionVector);

            FastVector WekaAttributes = new FastVector(NUMBER_OF_ATTRIBUTES);
            WekaAttributes.add(context);
            WekaAttributes.add(weather);
            WekaAttributes.add(backspace_frequency);
            WekaAttributes.add(erased_length);
            WekaAttributes.add(time);
            WekaAttributes.add(typing_speed);
            WekaAttributes.add(touch_count);
            WekaAttributes.add(emotion);

            Instances predict = new Instances("near",WekaAttributes,NUMBER_OF_INSTANCES);
            predict.setClassIndex(7);

            Instance example = new DenseInstance(NUMBER_OF_ATTRIBUTES);
            example.setValue(context,Mcontext);
            example.setValue(weather,Mweather);
            example.setValue(backspace_frequency,MbackspaceFrequency);
            example.setValue(erased_length,MerasedTextLength);
            example.setValue(time,MpartOfTheDay);
            example.setValue(typing_speed,MtypingSpeed);
            example.setValue(touch_count,MtouchCount);
            example.setValue(emotion,"negative");
            predict.add(example);

            AssetManager assetManager = ApplicationLoader.applicationContext.getAssets();
            InputStream is = assetManager.open("J48_74.model");

            Log.i("inputstream", String.valueOf(is));

            Classifier classifier = (Classifier) SerializationHelper.read(is);
            double prediction = classifier.classifyInstance(predict.instance(0));
            Log.i("mmm", String.valueOf(prediction));
            predizione = predict.classAttribute().value((int)prediction);
            Log.i("predizione",predizione);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return predizione;

    }
    }

