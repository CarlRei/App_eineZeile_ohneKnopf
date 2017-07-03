package com.example.carlreichert.stakk_meth_1;

        import android.animation.ObjectAnimator;
        import android.content.Context;
        import android.content.Intent;
        import android.content.res.Resources;
        import android.graphics.Color;
        import android.graphics.Point;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.Parcelable;
        import android.os.Vibrator;

        import android.support.annotation.RequiresApi;
        import android.support.v4.content.ContextCompat;
        import android.support.v7.app.ActionBar;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.view.ContextThemeWrapper;
        import android.support.v7.widget.LinearLayoutCompat;
        import android.util.DisplayMetrics;
        import android.util.Log;
        import android.view.Display;
        import android.view.Gravity;
        import android.view.MenuItem;
        import android.view.MotionEvent;
        import android.view.View;
        import android.view.ViewGroup;
        import android.view.ViewTreeObserver;
        import android.widget.Button;
        import android.widget.FrameLayout;
        import android.widget.LinearLayout;
        import android.widget.RelativeLayout;
        import android.widget.ScrollView;
        import android.widget.TextView;
        import android.widget.Toast;


        import org.w3c.dom.Document;
        import org.w3c.dom.Element;
        import org.w3c.dom.Node;
        import org.w3c.dom.NodeList;
        import org.w3c.dom.Text;
        import org.xml.sax.SAXException;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.util.ArrayList;
        import java.util.List;

        import javax.xml.parsers.DocumentBuilder;
        import javax.xml.parsers.DocumentBuilderFactory;
        import javax.xml.parsers.ParserConfigurationException;

        import static android.view.Gravity.BOTTOM;
        import static java.lang.Thread.sleep;
        import static java.security.AccessController.getContext;
//import android.app.ActionBar;

public class Textseite extends AppCompatActivity {
    double sizeInPx;
    List<TextView> vowels = new ArrayList<TextView>();
    int score;
    int hiddenScore;
    int screenWidth;
    List<TextView> hitboxes = new ArrayList<TextView>();


//#################################################################################################
// LifeCycle
//#################################################################################################

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textseite);

        double density = getApplicationContext().getResources().getDisplayMetrics().densityDpi;

        sizeInPx = 20/(72/(density)); // font size in px

        score = 0;
        hiddenScore = 0;
        TextView scoreView = (TextView)findViewById(R.id.score);
        scoreView.setText(Integer.toString(score));

        final Bundle intendAuswertung = getIntent().getExtras();
        if (intendAuswertung != null){
            setPageHead();
            buildingTextLayout(readFile(intendAuswertung.getString("Uebergabetext")));
            //buildingTextLayout(writeText(parseDoc("d")));
            setPageFoot();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        super.onWindowFocusChanged(hasFocus);

        if(hasFocus) {
            hitboxManagement();
            scrollManagement();
        }
    }

//_________________________________________________________________________________________________
// Hauptmethoden

    private void buildingTextLayout(String text){
        char curr = 'a';
        LinearLayout currRow = setRowLayout();
        LinearLayout currWord = setWordLayout();
        int btnNumber = 0;
        int rowWidth = 0;
        int wordWidth = 0;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;

        for(int i = 0; i < text.length(); i++){
            curr = text.charAt(i);
            TextView tv = null;

            if (checkIfVowel(curr)){
                if (checkIfVowel(text.charAt(i+1))){
                    tv = prepDoubleVowel(new StringBuilder().append(curr).append(text.charAt(i+1)).toString(), btnNumber);
                    i++;
                } else {
                    tv = prepVowel(curr, btnNumber);
                }
                btnNumber++;
            } else{
                tv = setView(curr);
            }

            if(curr == ' ' || i == text.length() - 1){

                if(rowWidth + wordWidth > screenWidth - 400){
                    LinearLayout newRow = setRowLayout();
                    currRow = newRow;
                    rowWidth = wordWidth;

                } else {
                    currWord.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    tv.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    rowWidth += currWord.getMeasuredWidth();
                    rowWidth += tv.getMeasuredWidth();
                }
                currRow.addView(currWord);
                currRow.addView(tv);

                LinearLayout newWord = setWordLayout();
                currWord = newWord;
                wordWidth = 0;
            } else {
                currWord.addView(tv);
                tv.measure(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                wordWidth += tv.getMeasuredWidth();
            }
        }

    }

    private void scrollManagement(){

        final ScrollView sv = (ScrollView) findViewById(R.id.sv);
        int numberOfAbove = 0;
        disableManuallyScrolling(sv);

        for (int i = 0; i < vowels.size(); i++){
            final int i_forClick = i;
            if((i + 1) < vowels.size() && vowels.get(i).getParent().getParent() != vowels.get(i + 1).getParent().getParent()){
                numberOfAbove++;
                final int finalNumberOfAbove = numberOfAbove;
                hitboxes.get(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myScrollTo(sv, finalNumberOfAbove);
                        increaseHitbox(i_forClick + 1);
                        addScore();
                        disablePrevBtns(i_forClick);
                        punisher(i_forClick);

                        vowels.get(i_forClick).setBackgroundColor(Color.LTGRAY);
                    }
                });
            } else if ((i + 1) < vowels.size()){
                final int finalNumberOfAbove = numberOfAbove;
                hitboxes.get(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        increaseHitbox(i_forClick + 1);
                        addScore();
                        disablePrevBtns(i_forClick);
                        vowels.get(i_forClick).setBackgroundColor(Color.LTGRAY);


                        int[] pos = new int[2];
                        vowels.get(i_forClick).getLocationInWindow(pos);
                        DisplayMetrics metrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(metrics);
                        if(pos[1] != (((metrics.heightPixels - vowels.get(i_forClick).getHeight())/2) + 50)){ //scroll when one of the vowels in next rows are clicked too early
                            myScrollTo(sv, finalNumberOfAbove);
                        }
                        punisher(i_forClick);
                    }
                });
            } else{
                hitboxes.get(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        vowels.get(i_forClick).setBackgroundColor(Color.LTGRAY);
                        addScore();
                        disablePrevBtns(i_forClick);

                        punisher(i_forClick);
                    }
                });
            }
        }
    }

    private void hitboxManagement(){

        int[] pos = new int[2];

        for (int i = 0; i < vowels.size(); i++) {
            /*
            Log.d("Pips", " ");
            Log.d("Pips", "getLocationInWindow X = " + pos[0]);
            Log.d("Pips", "getLocationInWindow Y = " + pos[1]); */
            setHitbox(vowels.get(i));


        }
    }

//_________________________________________________________________________________________________
// LayoutSetter

    private void setHitbox(TextView vowel){

        int[] pos = new int[2];
        int hbWidth = Math.max(vowel.getWidth(), 100);
        vowel.getLocationInWindow(pos);

        TextView hb = new TextView(this);

        RelativeLayout.LayoutParams hbParams = new RelativeLayout.LayoutParams(hbWidth, vowel.getHeight());
        //hb.setBackgroundColor(Color.BLUE);



        hbParams.leftMargin = pos[0] - ((hbWidth - vowel.getWidth())/2);
        hbParams.topMargin = pos[1] - 50;

        RelativeLayout hitboxLayout = (RelativeLayout) findViewById(R.id.hitboxLayout);
        hitboxLayout.addView(hb, hbParams);

        hitboxes.add(hb);
    }

    private TextView setView(char letter) {
        TextView tv = new TextView(this);
        String res = Character.toString(letter);
        LinearLayout.LayoutParams twParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        if(letter == ' '){
            res = "  ";
        }

        tv.setLayoutParams(twParams);
        tv.setTextSize((int) Math.ceil(sizeInPx));//
        tv.setText(res);

        return tv;
    }

    private LinearLayout setWordLayout() {

        LinearLayout linLay =new LinearLayout(this);

        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        linLay.setGravity(Gravity.CENTER_VERTICAL);
        linLay.setLayoutParams(llParams);
        linLay.setOrientation(LinearLayout.HORIZONTAL);

        return linLay;
    }

    private LinearLayout setRowLayout(){

        LinearLayout overAll = (LinearLayout) findViewById(R.id.overAll);
        LinearLayout linLay =new LinearLayout(this);

        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(screenWidth - 200,
                (int) (sizeInPx * 4.5)
        );
        linLay.setGravity(Gravity.CENTER_VERTICAL);
        linLay.setLayoutParams(llParams);
        linLay.setOrientation(LinearLayout.HORIZONTAL);
        llParams.setMargins(100, 0, 100, 0);

        overAll.addView(linLay);

        return linLay;
    }

    private TextView prepVowel(char vowel, int btnNumber){

        TextView btn = new TextView(this);
        btn.setId(btnNumber);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btn.setLayoutParams(btnParams);

        btn.setBackgroundColor(Color.parseColor("#FCA55D"));
        btn.setTextSize((int) Math.ceil(sizeInPx));
        btn.setText(Character.toString(vowel));
        btn.setTransformationMethod(null);
        btn.setGravity(Gravity.CENTER); // set the minimal Width of hitboxes
        btn.setMinWidth((int) (Math.ceil(sizeInPx))/3);

        vowels.add(btn);

        return btn;
    }

    private TextView prepDoubleVowel(String doubleVowel, int btnNumber){

        TextView btn = new TextView(this);
        btn.setId(btnNumber);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btn.setLayoutParams(tvParams);

        btn.setBackgroundColor(Color.parseColor("#FCA550"));
        btn.setTextSize((int) Math.ceil(sizeInPx));
        btn.setText(doubleVowel);
        btn.setTransformationMethod(null);

        vowels.add(btn);

        return btn;
    }

    private void setPageHead(){
        TextView blankHead = new TextView(this);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) ((metrics.heightPixels - (sizeInPx * 4.5))/2));
        viewParams.rightMargin = 10;

        TextView scoreView = (TextView)findViewById(R.id.score);
        scoreView.setLayoutParams(viewParams);
        blankHead.setLayoutParams(viewParams);

        LinearLayout overAll = (LinearLayout) findViewById(R.id.overAll);
        overAll.addView(blankHead);


        TextView between = (TextView) findViewById(R.id.between);

        LinearLayout.LayoutParams params =  new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (sizeInPx * 4.5));

        params.height = (int) (sizeInPx * 4.5);

        between.setLayoutParams(params);

    }

    private void setPageFoot(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) ((metrics.heightPixels - (sizeInPx * 4.5))/2));

        TextView footer = (TextView)findViewById(R.id.foot);
        footer.setLayoutParams(viewParams);

        TextView blankFoot = new TextView(this);
        blankFoot.setLayoutParams(viewParams);

        LinearLayout overAll = (LinearLayout) findViewById(R.id.overAll);
        overAll.addView(blankFoot);


    }

//_________________________________________________________________________________________________
// Textbereitung

    private String writeText(Document doc) {
        String finalText = "";
        NodeList wortList = doc.getElementsByTagName("wort");
        for (int i = 0; i < wortList.getLength(); i++){
            Node p = wortList.item(i);
            if(p.getNodeType()== Node.ELEMENT_NODE){
                Element wort = (Element) p;
                String wortName = wort.getAttribute("name");
                finalText += wortName + " ";
            }
        }
        return finalText;
    }

    private Document parseDoc(String docName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = null;

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(getApplicationContext().getResources().openRawResource(R.raw.meineerstexml));
        } catch (ParserConfigurationException e) {
            //tv.setText("etwas ist schief gegangen 1");
            e.printStackTrace();
        } catch (SAXException e) {
            //tv.setText("etwas ist schief gegangen 2");
            e.printStackTrace();
        } catch (IOException e) {
            //tv.setText("etwas ist schief gegangen 3");
            e.printStackTrace();
        }
        return doc;
    }

    private String readFile(String fileName){
        StringBuilder text = new StringBuilder();
        Resources res = this.getResources();
        int textId = res.getIdentifier(fileName, "raw", this.getPackageName());
        InputStream is = getApplicationContext().getResources().openRawResource(textId);
        BufferedReader bRead = new BufferedReader (new InputStreamReader(is));
        String line = "";

        try {
            while((line = bRead.readLine()) != null){
                text.append(line);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text.toString();
    }

//_________________________________________________________________________________________________
// kleine Hilfsmethoden

    private void disablePrevBtns(int currBtn){ //disables the previous buttons, that hasn't been clicked yet, so if one btn was skipped,
        //it cannot be tapped after the next one was tapped.
        for(int i  = 0; i <= currBtn + 1 - score; i++){
            hitboxes.get(currBtn - i).setClickable(false);
        }
    }

    private void addScore(){
        score++;
        TextView scoreView = (TextView)findViewById(R.id.score);
        scoreView.setText(Integer.toString(score));
    }

    private void myScrollTo(final ScrollView sv, final int numberOfAbove){
        sv.post(new Runnable() {
            public void run() {
                //sv.smoothScrollBy(0, (int) (sizeInPt * 4.5));
                ObjectAnimator.ofInt(sv, "scrollY",  (int) (sizeInPx * 4.5 * numberOfAbove)).setDuration(600).start();
            }
        });
    }

    private void disableManuallyScrolling(ScrollView sv){
        sv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            } // here I disable the ability for the user to scroll manually
        });
    }

    private boolean checkIfVowel(char letter){
        boolean res = false;
        switch (letter){
            case 'a': res = true;
                break;
            case 'e':res = true;
                break;
            case 'i':res = true;
                break;
            case 'o':res = true;
                break;
            case 'u':res = true;
                break;
            case 'ä':res = true;
                break;
            case 'ö':res = true;
                break;
            case 'ü':res = true;
                break;
            case 'A':res = true;
                break;
            case 'E':res = true;
                break;
            case 'I':res = true;
                break;
            case 'O':res = true;
                break;
            case 'U':res = true;
                break;
            case 'Ä':res = true;
                break;
            case 'Ö':res = true;
                break;
            case 'Ü':res = true;
                break;
            default: res = false;
                break;
        }
        return res;
    }

    private void increaseHitbox(int nextHb){
        ScrollView sv = (ScrollView) findViewById(R.id.sv);

        int hbWidth = Math.max(hitboxes.get(nextHb).getWidth(), 200);

        RelativeLayout.LayoutParams hbParams = new RelativeLayout.LayoutParams(hbWidth, hitboxes.get(nextHb).getHeight());

        int[] pos = new int[2];
        hitboxes.get(nextHb).getLocationInWindow(pos);

        hbParams.leftMargin = pos[0] - ((hbWidth - hitboxes.get(nextHb).getWidth())/2);
        hbParams.topMargin = pos[1] - 50 + sv.getScrollY();

        hitboxes.get(nextHb).setLayoutParams(hbParams);
        vowels.get(0).getLocationInWindow(pos);
    }

    private void punisher(int vowelNumber){
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        if (hiddenScore != vowelNumber){
            v.vibrate(300);

            for (int i = hiddenScore; i < vowelNumber; i ++){
                vowels.get(i).setBackgroundColor(Color.parseColor("#F5A9A9"));
            }

            final LinearLayout topMask = (LinearLayout) findViewById(R.id.head);
            final TextView botMask = (TextView) findViewById(R.id.foot);
            final Context context = this;

            topMask.setBackground(ContextCompat.getDrawable(this, R.drawable.gradient_selector3));
            botMask.setBackground(ContextCompat.getDrawable(this, R.drawable.gradient_selector4));

            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    topMask.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient_selector));
                    botMask.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient_selector2));
                }
            }, 300);
        } else {
            v.vibrate(40);
        }
        hiddenScore = vowelNumber + 1;
    }
}
