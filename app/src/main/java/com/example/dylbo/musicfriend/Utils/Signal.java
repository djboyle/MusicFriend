package com.example.dylbo.musicfriend.Utils;


import android.content.Context;
import android.widget.ArrayAdapter;

import com.example.dylbo.musicfriend.R;

import java.lang.reflect.Field;
//import android.content.res.AssetManager;//Trop long d'acceder à un asset, il faut passer par les raw
import java.io.InputStream;
//import java.io.IOException;
import java.lang.Exception;

//import android.util.Log;

/** This class is keystone.
 * Produce a byte[] (8bits) clic+silence that we can feed to a 16bits PCM AudioTrack buffer.
 * The clic is either generated or read from resources (raw is faster than assests because raw is included in the binary whereas assets exist as separated files in /data/data/com.arnaud.metronome/ and are longer to use.
 * The clic+silence means we keep feeding audioTrack to death instead of feeding a clic then pausing. This way we avoid android non-rt fonction calling and get a reliable metronome.
 * Basically it's like audioTrack is reading from a file of pre-recorder clics. So yeah pretty reliable.
 * Credit goes to masterex for the idea.
 */

public class Signal
{
    static byte [] getSignal (Context context, int clicChoice, float clicFreq, int clicSize, int sampleRate, double bpm)
    {
        switch (clicChoice) {
            case 0:
                return convert(generate(clicFreq,clicSize,sampleRate,bpm));
            case 1:
                return convert(generate(clicFreq,0,sampleRate,bpm));
            default:
                return fromRaw(context,clicChoice,sampleRate,bpm);
        }
    }

    static byte [] fromRaw (Context context, int clicChoice, int sampleRate, double bpm)
    {
        int periodSize = (int) ((60.*sampleRate)/bpm);
        int clicSize = 0;
        byte [] sig = new byte[2*periodSize]; // Each 16bits sample takes 2 bytes to be represented

        /* I wanted to read from Assets instead of raw because easier. We don't need R.id and I prefer to use a filename instead of a int. Unfortunately, The problem is asset is slow... raw is not! */
        try {
            //InputStream is = context.getAssets().open("clics/"+context.getAssets().list("clics")[id], AssetManager.ACCESS_BUFFER);//From assets too slow
			/*ArrayAdapter<CharSequence> adapterClics = ArrayAdapter.createFromResource(context,R.array.menuSettingsListClics, android.R.layout.simple_spinner_item);
			String clicName = adapterClics.getItem(clicChoice).toString();
			int id = context.getResources().getIdentifier(clicName,"raw","com.arnaud.metronome");*///From raw but too convoluted

            //Here, easy way.
            Field [] fields = R.raw.class.getFields();
            int id = fields[clicChoice-2].getInt(null);

            InputStream is = context.getResources().openRawResource(id);//From raw
            clicSize = is.read(sig,0,periodSize);// We read the file and eventually crop it to half the periodSize (so 2*periodSize/2). Remember that sound[] contains demi-samples (array of 8bits elements representting a 16bits signal)!!
            is.close();
        } catch (Exception e) {
            //catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        // Add Silence
        for (int i=clicSize;i<2*periodSize;i++)
            sig[i] = 0;

        return sig;
    }

    static double[] generate (double freq, int clicSize, int sampleRate, double bpm)
    {
        int periodSize = (int) ((60.*sampleRate)/bpm);
        double wStep = (2*Math.PI*freq)/sampleRate;//Pulsation steps
        double [] sig = new double[periodSize];

        //Convert clicSize (ms) to samples and crop it to half period if bpm too high
        clicSize = (clicSize*sampleRate)/1000;//integer division
        if (clicSize >= periodSize)
            clicSize = periodSize/2;

        //Build the whole signal: tic+silence
        //Tic
        for (int i=0;i<clicSize;i++)
            sig[i] = Math.sin(i*wStep);
        //Silence
        for (int i=clicSize;i<periodSize;i++)
            sig[i] = 0;

        return sig;
    }

    static byte [] convert (double [] sigle)
    {
        /* From [-1,1] Little-endian to [-2^15,2^15-1] big-endian */
        /* On utilise un tableau de byte (donc 8bits ex: 0xff) pour nourrir le buffer audio. Le système audio configuré en 16bits (ex: 0xaadd) lira 2 éléments pour reconstruire un échantillon */
        /* sigbe doit donc être 2 fois plus grand que signal */
        byte[] sigbe = new byte[2*sigle.length];
        int i = 0;

        /* Les Short font 16bits et sont signés (gràce au 16ème bit). MAX = 2^15-1 (0 enlève une valeur possible) et MIN = -2^15 (le zéro étant déjà géré)  */
        for (double sample : sigle) { // scale to maximum amplitude
            /* On normalise notre signal: de [-1,1] à [-Short.MAX_VALUE,Short.MAX_VALUE] */
            short normalizedSample = (short) ((sample*Short.MAX_VALUE));
            // in 16 bit wav PCM, first byte is the low order byte
            /* Ça veut dire qu'un échantillon = (short) 0xaadd sera représenté en PCM 16bits (byte)0xdd (byte) 0xaa */
            sigbe[i] = (byte) (normalizedSample & 0x00ff); //0xaadd & 0x00ff = 0x00dd; (byte) 0x00dd = 0xdd; On a isolé le byte d'ordre faible
            i++;
            sigbe[i] = (byte) ((normalizedSample & 0xff00) >>> 8); //0xaadd & 0xff00 = 0xaa00; (byte) 0xaa00 = 0xaa; On a isolé le byte d'ordre élevé
            i++;
        }
        return sigbe;
    }
}
