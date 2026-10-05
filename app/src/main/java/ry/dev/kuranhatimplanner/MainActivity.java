package ry.dev.kuranhatimplanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    // Farben: warmes Papier, dunkle Tinte, ein einziger grüner Akzent
    private static final int BG = 0xFFF7F4EE, SURFACE = 0xFFFFFFFF, INK = 0xFF1E2622, MUTED = 0xFF7A8178,
            LINE = 0xFFDFDACD, ACCENT = 0xFF245C48, TINT = 0xFFE6EDE5, DANGER = 0xFF9A3B32;
    private static final int MAX = 30;
    private static final String AD_UNIT_ID = BuildConfig.ADMOB_AD_UNIT_ID;
    private static final int[] COUNTS = {5, 6, 10, 15, 30};
    private static final String[] LANG_CODES = {"de", "en", "tr", "ar"};
    private static final String[] LANG_LABELS = {"Deutsch", "English", "T\u00FCrk\u00E7e", "\u0627\u0644\u0639\u0631\u0628\u064a\u0629"};
    private static final String[] DE_MONTHS = {"Januar", "Februar", "M\u00E4rz", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"};
    private static final String[] DE_SHORT_MONTHS = {"Jan.", "Feb.", "M\u00E4rz", "Apr.", "Mai", "Juni", "Juli", "Aug.", "Sept.", "Okt.", "Nov.", "Dez."};
    private static final String[] EN_MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private static final String[] EN_SHORT_MONTHS = {"Jan.", "Feb.", "Mar.", "Apr.", "May", "Jun.", "Jul.", "Aug.", "Sep.", "Oct.", "Nov.", "Dec."};
    private static final String[] TR_MONTHS = {"Ocak", "\u015Eubat", "Mart", "Nisan", "May\u0131s", "Haziran", "Temmuz", "A\u011Fustos", "Eyl\u00FCl", "Ekim", "Kas\u0131m", "Aral\u0131k"};
    private static final String[] TR_SHORT_MONTHS = {"Oca", "\u015Eub", "Mar", "Nis", "May", "Haz", "Tem", "A\u011Fu", "Eyl", "Eki", "Kas", "Ara"};
    private static final String[] AR_MONTHS = {"\u064A\u0646\u0627\u064A\u0631", "\u0641\u0628\u0631\u0627\u064A\u0631", "\u0645\u0627\u0631\u0633", "\u0623\u0628\u0631\u064A\u0644", "\u0645\u0627\u064A\u0648", "\u064A\u0648\u0646\u064A\u0648", "\u064A\u0648\u0644\u064A\u0648", "\u0623\u063A\u0633\u0637\u0633", "\u0633\u0628\u062A\u0645\u0628\u0631", "\u0623\u0643\u062A\u0648\u0628\u0631", "\u0646\u0648\u0641\u0645\u0628\u0631", "\u062F\u064A\u0633\u0645\u0628\u0631"};
    private static final String[] AR_SHORT_MONTHS = {"\u064A\u0646\u0627", "\u0641\u0628\u0631", "\u0645\u0627\u0631", "\u0623\u0628\u0631", "\u0645\u0627\u064A", "\u064A\u0648\u0646", "\u064A\u0648\u0644", "\u0623\u063A\u0633", "\u0633\u0628\u062a", "\u0623\u0643\u062a", "\u0646\u0648\u0641", "\u062F\u064A\u0633"};
    private static final String[] TR = {"Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"};
    private static final String[] TR_SHORT = {"Oca", "Şub", "Mar", "Nis", "May", "Haz", "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara"};

    private final Typeface MEDIUM = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    private final Typeface SERIF = Typeface.create("serif", Typeface.NORMAL);
    private final Typeface SERIF_BOLD = Typeface.create("serif", Typeface.BOLD);

    private final EditText[] nameFields = new EditText[MAX];
    private final TextView[] countChips = new TextView[COUNTS.length];
    private final ArrayList<Calendar> pauseMonths = new ArrayList<>();
    private final ArrayList<GroupData> groups = new ArrayList<>();

    private ScrollView scroll;
    private AdView adView;
    private LinearLayout drawerGroups, pauseBox, resultBox, namesBox;
    private TextView startButton, barTitle;
    private View scrim;
    private ScrollView drawerPanel;
    private boolean drawerOpen = false;
    private int drawerW;
    private int currentGroupIndex = 0, personCount = 15;
    private Calendar selectedStart;
    private ArrayList<String> currentNames;
    private ArrayList<MonthPlan> currentPlan;
    private String language = "de";

    // ------------------------------------------------------------------ Lebenszyklus

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if (getActionBar() != null) getActionBar().hide();
        language = getPreferences(0).getString("language", "de");
        selectedStart = Calendar.getInstance();
        selectedStart.set(Calendar.DAY_OF_MONTH, 1);
        groups.add(new GroupData("Kuran Hatim 1"));
        // Alle 30 Felder sofort anlegen, damit beim Laden keine Namen verloren gehen
        for (int i = 0; i < MAX; i++) nameFields[i] = newInput("Name");
        buildUi();
        loadGroups();
    }

    @Override
    protected void onPause() {
        if (adView != null) adView.pause();
        super.onPause();
        saveGroups();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) adView.resume();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) adView.destroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (drawerOpen) {
            closeDrawer();
            return;
        }
        super.onBackPressed();
    }

    // ------------------------------------------------------------------ Hilfsfunktionen für das Design

    private int dp(float n) {
        return (int) (n * getResources().getDisplayMetrics().density + .5f);
    }

    private TextView text(String s, float sp, int color, Typeface tf) {
        TextView v = new TextView(this);
        v.setText(localizeText(s));
        v.setTextSize(sp);
        v.setTextColor(color);
        if (tf != null) v.setTypeface(tf);
        return v;
    }

    private void margin(View v, int l, int t, int r, int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(dp(l), dp(t), dp(r), dp(b));
        v.setLayoutParams(p);
    }

    private GradientDrawable shape(int fill, int stroke, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        if (stroke != 0) g.setStroke(Math.max(1, dp(.75f)), stroke);
        return g;
    }

    private Drawable ripple(Drawable content, int color) {
        return new RippleDrawable(ColorStateList.valueOf(color), content, null);
    }

    private void styleField(TextView v) {
        v.setTextSize(16);
        v.setTextColor(INK);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(dp(14), 0, dp(14), 0);
        v.setBackground(shape(SURFACE, LINE, 10));
    }

    private EditText newInput(String hint) {
        EditText e = new EditText(this);
        e.setHint(localizeText(hint));
        e.setHintTextColor(0xFFB3B8AD);
        e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        e.setSingleLine(true);
        styleField(e);
        return e;
    }

    private TextView button(String s, boolean primary) {
        TextView b = text(s, 15, primary ? Color.WHITE : ACCENT, MEDIUM);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(16), 0, dp(16), 0);
        b.setBackground(ripple(primary ? shape(ACCENT, 0, 12) : shape(SURFACE, LINE, 12),
                primary ? 0x33FFFFFF : 0x1A245C48));
        return b;
    }

    private TextView link(String s, int color) {
        TextView v = text(s, 14, color, MEDIUM);
        v.setPadding(0, dp(12), 0, dp(12));
        return v;
    }

    private void section(LinearLayout p, String s) {
        if (s.contains("Anzahl")) s = t("people");
        else if (s.equals("Namen")) s = t("names");
        else if (s.contains("Startmonat")) s = t("start");
        else if (s.contains("Pausenmonate")) s = t("pauses");
        else if (s.contains("Auswertung")) s = t("evaluation");
        TextView v = text(s.toUpperCase(Locale.GERMANY), 11.5f, MUTED, MEDIUM);
        v.setLetterSpacing(.08f);
        margin(v, 0, 24, 0, 8);
        p.addView(v);
    }

    private AlertDialog.Builder dialog() {
        return new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
    }

    private void toast(String s) {
        Toast.makeText(this, localizeText(s), Toast.LENGTH_LONG).show();
    }

    private String t(String key) {
        String[][] values = {
                {"intro", "Monatliche C\u00FCz-Einteilung", "Monthly C\u00FCz assignment", "Ayl\u0131k c\u00FCz da\u011F\u0131l\u0131m\u0131", "\u062a\u0648\u0632\u064a\u0639 \u0627\u0644\u0623\u062c\u0632\u0627\u0621 \u0627\u0644\u0634\u0647\u0631\u064a"},
                {"people", "Anzahl Personen", "Number of people", "Ki\u015Fi say\u0131s\u0131", "\u0639\u062f\u062f \u0627\u0644\u0623\u0634\u062e\u0627\u0635"},
                {"names", "Namen", "Names", "\u0130simler", "\u0627\u0644\u0623\u0633\u0645\u0627\u0621"},
                {"start", "Startmonat", "Start month", "Ba\u015Flang\u0131\u00E7 ay\u0131", "\u0634\u0647\u0631 \u0627\u0644\u0628\u062f\u0627\u064a\u0629"},
                {"pause_month", "Pausenmonat", "Pause month", "Ara ay\u0131", "\u0634\u0647\u0631 \u0627\u0644\u062a\u0648\u0642\u0641"},
                {"pauses", "Pausenmonate", "Pause months", "Ara verilen aylar", "\u0623\u0634\u0647\u0631 \u0627\u0644\u062a\u0648\u0642\u0641"},
                {"add_pause", "+  Pausenmonat hinzuf\u00FCgen", "+  Add pause month", "+  Ara ay\u0131 ekle", "+  \u0625\u0636\u0627\u0641\u0629 \u0634\u0647\u0631 \u062a\u0648\u0642\u0641"},
                {"pause_note", "Pausenmonate z\u00E4hlen nicht als Vergabemonat.", "Pause months do not count as assignment months.", "Ara verilen aylar da\u011F\u0131t\u0131m ay\u0131 olarak say\u0131lmaz.", "\u0623\u0634\u0647\u0631 \u0627\u0644\u062a\u0648\u0642\u0641 \u0644\u0627 \u062a\u064f\u062d\u062a\u0633\u0628 \u0643\u0623\u0634\u0647\u0631 \u062a\u0648\u0632\u064a\u0639."},
                {"create_plan", "Plan erstellen", "Create plan", "Plan\u0131 olu\u015Ftur", "\u0625\u0646\u0634\u0627\u0621 \u0627\u0644\u062e\u0637\u0629"},
                {"evaluation", "Auswertung", "Overview", "De\u011Ferlendirme", "\u0627\u0644\u0646\u062a\u0627\u0626\u062c"},
                {"overview_image", "\u00DCbersicht als Bild", "Overview as image", "Genel g\u00F6r\u00FCn\u00FCm\u00FC resim olarak payla\u015F", "\u0645\u0634\u0627\u0631\u0643\u0629 \u0627\u0644\u0646\u0638\u0631\u0629 \u0627\u0644\u0639\u0627\u0645\u0629 \u0643\u0635\u0648\u0631\u0629"},
                {"overview_pdf", "\u00DCbersicht als PDF", "Overview as PDF", "Genel g\u00F6r\u00FCn\u00FCm\u00FC PDF olarak payla\u015F", "\u0645\u0634\u0627\u0631\u0643\u0629 \u0627\u0644\u0646\u0638\u0631\u0629 \u0627\u0644\u0639\u0627\u0645\u0629 \u0643\u0645\u0644\u0641 PDF"},
                {"share_image", "Als Bild teilen", "Share as image", "Resim olarak payla\u015F", "\u0645\u0634\u0627\u0631\u0643\u0629 \u0643\u0635\u0648\u0631\u0629"},
                {"pause", "Pause", "Pause", "Ara", "\u062a\u0648\u0642\u0641"},
                {"remove", "Entfernen", "Remove", "Kald\u0131r", "\u0625\u0632\u0627\u0644\u0629"},
                {"group_name", "Gruppenname", "Group name", "Grup ad\u0131", "\u0627\u0633\u0645 \u0627\u0644\u0645\u062c\u0645\u0648\u0639\u0629"},
                {"rename_group", "Gruppe umbenennen", "Rename group", "Grubu yeniden adland\u0131r", "\u0625\u0639\u0627\u062f\u0629 \u062a\u0633\u0645\u064a\u0629 \u0627\u0644\u0645\u062c\u0645\u0648\u0639\u0629"},
                {"cancel", "Abbrechen", "Cancel", "\u0130ptal", "\u0625\u0644\u063a\u0627\u0621"},
                {"save", "Speichern", "Save", "Kaydet", "\u062d\u0641\u0638"},
                {"clear_content", "Inhalt l\u00F6schen?", "Clear content?", "\u0130\u00E7erik silinsin mi?", "\u0645\u0633\u062d \u0627\u0644\u0645\u062d\u062a\u0648\u0649\u061f"},
                {"last_group", "Die letzte Gruppe bleibt bestehen, wird aber auf einen leeren Neuzustand zur\u00FCckgesetzt.", "The last group will remain but be reset to an empty new state.", "Son grup kalacak ancak yeni ve bo\u015F duruma s\u0131f\u0131rlanacak.", "\u0633\u062a\u0628\u0642\u0649 \u0627\u0644\u0645\u062c\u0645\u0648\u0639\u0629 \u0627\u0644\u0623\u062e\u064a\u0631\u0629 \u0648\u0644\u0643\u0646 \u0633\u062a\u064f\u0639\u0627\u062f \u0625\u0644\u0649 \u062d\u0627\u0644\u0629 \u062c\u062f\u064a\u062f\u0629 \u0641\u0627\u0631\u063a\u0629."},
                {"clear", "Leeren", "Clear", "Temizle", "\u0645\u0633\u062d"},
                {"delete_group", "Gruppe l\u00F6schen?", "Delete group?", "Grup silinsin mi?", "\u062d\u0630\u0641 \u0627\u0644\u0645\u062c\u0645\u0648\u0639\u0629\u061f"},
                {"delete_message", " wird dauerhaft gel\u00F6scht.", " will be permanently deleted.", " kal\u0131c\u0131 olarak silinecek.", " \u0633\u064a\u062a\u0645 \u062d\u0630\u0641\u0647\u0627 \u0646\u0647\u0627\u0626\u064a\u0627\u064b."},
                {"delete", "L\u00F6schen", "Delete", "Sil", "\u062d\u0630\u0641"},
                {"apply", "\u00DCbernehmen", "Apply", "Uygula", "\u062a\u0637\u0628\u064a\u0642"},
                {"all_names", "Bitte alle %d Namen eingeben.", "Please enter all %d names.", "L\u00FCtfen %d ismin tamam\u0131n\u0131 girin.", "\u064a\u0631\u062c\u0649 \u0625\u062f\u062e\u0627\u0644 \u062c\u0645\u064a\u0639 \u0627\u0644\u0623\u0633\u0645\u0627\u0621 (%d)."},
                {"share_image_chooser", "Bild teilen", "Share image", "Resmi payla\u015F", "\u0645\u0634\u0627\u0631\u0643\u0629 \u0627\u0644\u0635\u0648\u0631\u0629"},
                {"share_pdf_chooser", "PDF teilen oder drucken", "Share or print PDF", "PDF'yi payla\u015F veya yazd\u0131r", "\u0645\u0634\u0627\u0631\u0643\u0629 \u0623\u0648 \u0637\u0628\u0627\u0639\u0629 PDF"},
                {"image_error", "Bild konnte nicht gespeichert werden.", "Could not save image.", "Resim kaydedilemedi.", "\u062a\u0639\u0630\u0631 \u062d\u0641\u0638 \u0627\u0644\u0635\u0648\u0631\u0629."},
                {"pdf_error", "PDF konnte nicht erstellt werden.", "Could not create PDF.", "PDF olu\u015Fturulamad\u0131.", "\u062a\u0639\u0630\u0631 \u0625\u0646\u0634\u0627\u0621 PDF."},
                {"month_header", "Monat", "Month", "Ay", "\u0627\u0644\u0634\u0647\u0631"},
                {"cuz_header", "C\u00FCz", "C\u00FCz", "C\u00FCz", "\u0627\u0644\u062c\u0632\u0621"},
                {"language", "Sprache", "Language", "Dil", "\u0627\u0644\u0644\u063a\u0629"},
                {"groups", "Gruppen", "Groups", "Gruplar", "\u0627\u0644\u0645\u062c\u0645\u0648\u0639\u0627\u062a"},
                {"new_group", "Neue Gruppe", "New group", "Yeni grup", "\u0645\u062c\u0645\u0648\u0639\u0629 \u062c\u062f\u064a\u062f\u0629"},
                {"rename", "Umbenennen", "Rename", "Yeniden adland\u0131r", "\u0625\u0639\u0627\u062f\u0629 \u062a\u0633\u0645\u064a\u0629"},
                {"menu", "Men\u00FC", "Menu", "Men\u00FC", "\u0627\u0644\u0642\u0627\u0626\u0645\u0629"}
        };
        int index = language.equals("en") ? 2 : language.equals("tr") ? 3 : language.equals("ar") ? 4 : 1;
        for (String[] row : values) if (row[0].equals(key)) return row[index];
        return key;
    }

    private String conjunction() {
        return language.equals("tr") ? " ve " : language.equals("ar") ? " \u0648 " : language.equals("en") ? " and " : " und ";
    }

    private void setLanguage(String code) {
        if (code.equals(language)) return;
        language = code;
        saveGroups();
        recreate();
    }

    private String localizeText(String s) {
        if (s == null || s.isEmpty()) return s;
        if (s.startsWith("Monatliche")) return t("intro");
        if (s.startsWith("+  Pausenmonat")) return t("add_pause");
        if (s.startsWith("Pausenmonate")) return t("pause_note");
        if (s.contains("bersicht als Bild")) return t("overview_image");
        if (s.contains("bersicht als PDF")) return t("overview_pdf");
        if (s.equals("Plan erstellen")) return t("create_plan");
        if (s.equals("Pause")) return t("pause");
        if (s.equals("Entfernen")) return t("remove");
        if (s.equals("Als Bild teilen")) return t("share_image");
        if (s.equals("Gruppenname")) return t("group_name");
        if (s.equals("Cüz")) return t("cuz_header");
        if (s.startsWith("Bild konnte")) return t("image_error");
        if (s.startsWith("PDF konnte")) return t("pdf_error");
        if (s.equals("Name")) return language.equals("en") ? "Name" : language.equals("tr") ? "İsim" : language.equals("ar") ? "الاسم" : "Name";
        return s;
    }

    // ------------------------------------------------------------------ Oberfläche

    private void buildUi() {
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        boolean rtl = language.equals("ar");
        drawerW = Math.min(dp(320), (int) (getResources().getDisplayMetrics().widthPixels * 0.86f));

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);
        root.setLayoutDirection(rtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        // Hauptspalte mit schmaler Kopfzeile, Inhalt und Werbebanner
        final LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        root.addView(main, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPaddingRelative(dp(8), 0, dp(22), 0);
        FrameLayout menuButton = new FrameLayout(this);
        menuButton.setContentDescription(t("menu"));
        menuButton.setBackground(ripple(shape(Color.TRANSPARENT, 0, 24), 0x1A000000));
        menuButton.setOnClickListener(v -> openDrawer());
        menuButton.addView(new MenuIcon(this, INK, dp(2)), new FrameLayout.LayoutParams(dp(22), dp(22), Gravity.CENTER));
        bar.addView(menuButton, new LinearLayout.LayoutParams(dp(48), dp(48)));
        barTitle = text("", 20, INK, SERIF);
        barTitle.setSingleLine(true);
        barTitle.setEllipsize(TextUtils.TruncateAt.END);
        barTitle.setPaddingRelative(dp(6), 0, 0, 0);
        barTitle.setOnClickListener(v -> openDrawer());
        bar.addView(barTitle, new LinearLayout.LayoutParams(0, -2, 1));
        main.addView(bar, new LinearLayout.LayoutParams(-1, dp(56)));

        View hairline = new View(this);
        hairline.setBackgroundColor(LINE);
        main.addView(hairline, new LinearLayout.LayoutParams(-1, 1));

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(4), dp(22), dp(32));
        scroll.addView(page);
        main.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        section(page, t("people"));
        LinearLayout counts = new LinearLayout(this);
        for (int i = 0; i < COUNTS.length; i++) {
            final int at = i;
            TextView chip = text(String.valueOf(COUNTS[i]), 15, INK, MEDIUM);
            chip.setGravity(Gravity.CENTER);
            chip.setOnClickListener(v -> selectCount(at));
            countChips[i] = chip;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(44), 1);
            lp.setMargins(i == 0 ? 0 : dp(4), 0, i == COUNTS.length - 1 ? 0 : dp(4), 0);
            counts.addView(chip, lp);
        }
        page.addView(counts);
        updateCountSelector();

        section(page, t("names"));
        namesBox = new LinearLayout(this);
        namesBox.setOrientation(LinearLayout.VERTICAL);
        page.addView(namesBox);
        renderNameFields();

        section(page, "Startmonat / Başlangıç");
        startButton = text("", 16, INK, null);
        styleField(startButton);
        startButton.setOnClickListener(v -> chooseMonth(selectedStart, true));
        page.addView(startButton, new LinearLayout.LayoutParams(-1, dp(48)));

        section(page, "Pausenmonate");
        pauseBox = new LinearLayout(this);
        pauseBox.setOrientation(LinearLayout.VERTICAL);
        page.addView(pauseBox);
        TextView add = link("+  Pausenmonat hinzufügen", ACCENT);
        add.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, 1);
            chooseMonth(c, false);
        });
        margin(add, 0, 2, 0, 0);
        page.addView(add);
        TextView note = text("Pausenmonate zählen nicht als Vergabemonat.", 13, MUTED, null);
        margin(note, 0, 0, 0, 0);
        page.addView(note);

        TextView gen = button(t("create_plan"), true);
        gen.setOnClickListener(v -> generatePlan());
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(-1, dp(52));
        gp.setMargins(0, dp(24), 0, 0);
        page.addView(gen, gp);

        resultBox = new LinearLayout(this);
        resultBox.setOrientation(LinearLayout.VERTICAL);
        page.addView(resultBox);

        // Werbebanner fest am unteren Rand, das SDK wird vor dem ersten Laden initialisiert
        MobileAds.initialize(this, status -> { });
        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(AD_UNIT_ID);
        adView.loadAd(new AdRequest.Builder().build());
        LinearLayout adBar = new LinearLayout(this);
        adBar.setGravity(Gravity.CENTER);
        adBar.setBackgroundColor(BG);
        adBar.setMinimumHeight(dp(50));
        adBar.addView(adView);
        main.addView(adBar, new LinearLayout.LayoutParams(-1, -2));

        // Abdunkelung hinter dem Menü
        scrim = new View(this);
        scrim.setBackgroundColor(0x66141A17);
        scrim.setAlpha(0f);
        scrim.setVisibility(View.GONE);
        scrim.setOnClickListener(v -> closeDrawer());
        root.addView(scrim, new FrameLayout.LayoutParams(-1, -1));

        // Seitenmenü mit Gruppen und Sprache
        drawerPanel = new ScrollView(this);
        drawerPanel.setBackgroundColor(SURFACE);
        drawerPanel.setFillViewport(true);
        drawerPanel.setVerticalScrollBarEnabled(false);
        drawerPanel.setClickable(true);
        final LinearLayout drawer = new LinearLayout(this);
        drawer.setOrientation(LinearLayout.VERTICAL);
        drawerPanel.addView(drawer);
        drawerPanel.setTranslationX(closedX());
        root.addView(drawerPanel, new FrameLayout.LayoutParams(drawerW, -1, Gravity.LEFT));

        TextView brand = text("Kuran Hatim Planner", 26, INK, SERIF);
        margin(brand, 0, 0, 0, 2);
        drawer.addView(brand);
        TextView sub = text("Monatliche Cüz-Einteilung", 14, MUTED, null);
        margin(sub, 0, 0, 0, 8);
        drawer.addView(sub);

        section(drawer, t("groups"));
        drawerGroups = new LinearLayout(this);
        drawerGroups.setOrientation(LinearLayout.VERTICAL);
        drawer.addView(drawerGroups);
        renderGroupTabs();

        // Platzhalter schiebt den Sprachknopf ganz nach unten
        drawer.addView(new View(this), new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout langRow = new LinearLayout(this);
        langRow.setGravity(Gravity.CENTER_VERTICAL);
        langRow.setPadding(dp(14), 0, dp(14), 0);
        langRow.setBackground(ripple(shape(SURFACE, LINE, 12), 0x14000000));
        langRow.setOnClickListener(v -> showLanguageDialog());
        langRow.addView(text(t("language"), 15, INK, null), new LinearLayout.LayoutParams(0, -2, 1));
        TextView langValue = text(languageLabel(language), 15, ACCENT, MEDIUM);
        langValue.setText(languageLabel(language));
        langRow.addView(langValue, new LinearLayout.LayoutParams(-2, -2));
        LinearLayout.LayoutParams lrp = new LinearLayout.LayoutParams(-1, dp(52));
        lrp.setMargins(0, dp(24), 0, 0);
        drawer.addView(langRow, lrp);

        root.setOnApplyWindowInsetsListener((v, insets) -> {
            main.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            drawer.setPadding(dp(20), insets.getSystemWindowInsetTop() + dp(28), dp(20),
                    insets.getSystemWindowInsetBottom() + dp(24));
            return insets;
        });

        setContentView(root);
    }

    // ------------------------------------------------------------------ Seitenmenü

    // Das Menü bleibt in allen Sprachen links, auch im arabischen Rechts-nach-links-Layout
    private float closedX() {
        return -drawerW;
    }

    private void openDrawer() {
        if (drawerOpen) return;
        drawerOpen = true;
        View focus = getCurrentFocus();
        if (focus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            focus.clearFocus();
        }
        scrim.animate().cancel();
        scrim.setVisibility(View.VISIBLE);
        scrim.animate().alpha(1f).setDuration(200).start();
        drawerPanel.animate().translationX(0f).setDuration(220).start();
    }

    private void closeDrawer() {
        if (!drawerOpen) return;
        drawerOpen = false;
        scrim.animate().cancel();
        scrim.animate().alpha(0f).setDuration(180).withEndAction(() -> {
            if (!drawerOpen) scrim.setVisibility(View.GONE);
        }).start();
        drawerPanel.animate().translationX(closedX()).setDuration(200).start();
    }

    private String languageLabel(String code) {
        for (int i = 0; i < LANG_CODES.length; i++) if (LANG_CODES[i].equals(code)) return LANG_LABELS[i];
        return code;
    }

    // Popup zur Sprachauswahl. Die Liste ist immer links ausgerichtet, auch der arabische Eintrag.
    private void showLanguageDialog() {
        final AlertDialog[] ref = new AlertDialog[1];
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        box.setPadding(dp(16), dp(8), dp(16), 0);
        for (int i = 0; i < LANG_CODES.length; i++) {
            final String code = LANG_CODES[i];
            boolean active = code.equals(language);
            LinearLayout row = new LinearLayout(this);
            row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(14), 0, dp(14), 0);
            row.setBackground(ripple(shape(active ? TINT : Color.TRANSPARENT, 0, 12), 0x14000000));
            TextView name = text(LANG_LABELS[i], 16, active ? ACCENT : INK, active ? MEDIUM : null);
            name.setText(LANG_LABELS[i]);
            name.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            name.setTextDirection(View.TEXT_DIRECTION_LTR);
            name.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            name.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            row.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
            if (active) {
                View dot = new View(this);
                dot.setBackground(shape(ACCENT, 0, 5));
                row.addView(dot, new LinearLayout.LayoutParams(dp(10), dp(10)));
            }
            row.setOnClickListener(v -> {
                ref[0].dismiss();
                setLanguage(code);
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(52));
            lp.setMargins(0, 0, 0, dp(4));
            box.addView(row, lp);
        }
        ref[0] = dialog().setTitle(t("language")).setView(box)
                .setNegativeButton(t("cancel"), null).create();
        ref[0].show();
    }

    private void selectCount(int index) {
        if (COUNTS[index] == personCount) return;
        saveCurrentToGroup();
        personCount = COUNTS[index];
        updateCountSelector();
        renderNameFields();
        resultBox.removeAllViews();
    }

    private void updateCountSelector() {
        for (int i = 0; i < COUNTS.length; i++) {
            boolean on = COUNTS[i] == personCount;
            countChips[i].setTextColor(on ? Color.WHITE : INK);
            countChips[i].setBackground(shape(on ? ACCENT : SURFACE, on ? ACCENT : LINE, 10));
        }
    }

    private void renderNameFields() {
        if (namesBox == null) return;
        namesBox.removeAllViews();
        for (int i = 0; i < personCount; i++) {
            EditText f = nameFields[i];
            if (f.getParent() != null) ((ViewGroup) f.getParent()).removeView(f);
            f.setImeOptions(i == personCount - 1 ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT);
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            TextView num = text(String.valueOf(i + 1), 13, MUTED, null);
            num.setGravity(Gravity.CENTER);
            row.addView(num, new LinearLayout.LayoutParams(dp(28), -2));
            row.addView(f, new LinearLayout.LayoutParams(0, dp(48), 1));
            margin(row, 0, 3, 0, 3);
            namesBox.addView(row);
        }
    }

    // ------------------------------------------------------------------ Gruppen

    // Baut die Gruppenliste im Seitenmenü neu auf und aktualisiert den Titel in der Kopfzeile
    private void renderGroupTabs() {
        if (barTitle != null) barTitle.setText(groups.get(currentGroupIndex).title);
        if (drawerGroups == null) return;
        drawerGroups.removeAllViews();
        for (int i = 0; i < groups.size(); i++) {
            final int at = i;
            boolean active = i == currentGroupIndex;
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setBackground(ripple(shape(active ? TINT : Color.TRANSPARENT, 0, 12), 0x14000000));

            TextView name = text(groups.get(i).title, 16, active ? ACCENT : INK, active ? MEDIUM : null);
            name.setText(groups.get(i).title);
            name.setSingleLine(true);
            name.setEllipsize(TextUtils.TruncateAt.END);
            name.setPadding(dp(14), dp(13), dp(14), dp(active ? 4 : 13));
            item.addView(name, new LinearLayout.LayoutParams(-1, -2));

            if (active) {
                LinearLayout actions = new LinearLayout(this);
                actions.setPaddingRelative(dp(14), 0, dp(14), dp(8));
                TextView rename = text(t("rename"), 13, ACCENT, MEDIUM);
                rename.setPaddingRelative(0, dp(6), dp(20), dp(6));
                rename.setOnClickListener(v -> renameGroup(at));
                actions.addView(rename);
                TextView del = text(t("delete"), 13, DANGER, MEDIUM);
                del.setPadding(0, dp(6), 0, dp(6));
                del.setOnClickListener(v -> deleteGroup(at));
                actions.addView(del);
                item.addView(actions, new LinearLayout.LayoutParams(-1, -2));
            }

            item.setOnClickListener(v -> {
                switchGroup(at);
                closeDrawer();
            });
            item.setOnLongClickListener(v -> {
                renameGroup(at);
                return true;
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.setMargins(0, 0, 0, dp(4));
            drawerGroups.addView(item, lp);
        }

        TextView add = text("+  " + t("new_group"), 15, ACCENT, MEDIUM);
        add.setPadding(dp(14), dp(13), dp(14), dp(13));
        add.setBackground(ripple(shape(SURFACE, LINE, 12), 0x14245C48));
        add.setOnClickListener(v -> {
            addGroup();
            closeDrawer();
        });
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, -2);
        ap.setMargins(0, dp(6), 0, 0);
        drawerGroups.addView(add, ap);
    }

    private void addGroup() {
        saveCurrentToGroup();
        groups.add(new GroupData(nextGroupTitle()));
        currentGroupIndex = groups.size() - 1;
        loadGroup();
        renderGroupTabs();
        saveGroups();
    }

    // Vergibt die erste freie Nummer. Alte Titel mit "Planner" zählen als belegt,
    // damit keine Nummer doppelt vorkommt.
    private String nextGroupTitle() {
        int number = 1;
        while (true) {
            String wanted = "Kuran Hatim " + number;
            String legacy = "Kuran Hatim Planner " + number;
            boolean used = false;
            for (GroupData g : groups) {
                if (g.title.equals(wanted) || g.title.equals(legacy)) {
                    used = true;
                    break;
                }
            }
            if (!used) return wanted;
            number++;
        }
    }

    // Benennt gespeicherte Gruppen mit dem alten Standardnamen um und vermeidet dabei doppelte Titel
    private void migrateLegacyTitles() {
        String prefix = "Kuran Hatim Planner ";
        for (GroupData g : groups) {
            if (!g.title.matches("Kuran Hatim Planner \\d+")) continue;
            String candidate = "Kuran Hatim " + g.title.substring(prefix.length());
            boolean taken = false;
            for (GroupData o : groups) {
                if (o != g && o.title.equals(candidate)) {
                    taken = true;
                    break;
                }
            }
            g.title = taken ? nextGroupTitle() : candidate;
        }
    }

    private void renameGroup(int index) {
        final EditText e = newInput("Gruppenname");
        e.setText(groups.get(index).title);
        e.setSelectAllOnFocus(true);
        LinearLayout box = new LinearLayout(this);
        box.setPadding(dp(20), dp(8), dp(20), 0);
        box.addView(e, new LinearLayout.LayoutParams(-1, dp(48)));
        dialog().setTitle(t("rename_group")).setView(box)
                .setNegativeButton(t("cancel"), null)
                .setPositiveButton(t("save"), (d, w) -> {
                    String t = e.getText().toString().trim();
                    if (t.isEmpty()) return;
                    groups.get(index).title = t;
                    renderGroupTabs();
                    if (currentPlan != null && index == currentGroupIndex && resultBox.getChildCount() > 0) renderResults();
                    saveGroups();
                }).show();
    }

    private void deleteGroup(int index) {
        if (groups.size() == 1) {
            dialog().setTitle(t("clear_content"))
                    .setMessage(t("last_group"))
                    .setNegativeButton(t("cancel"), null)
                    .setPositiveButton(t("clear"), (d, w) -> {
                        GroupData g = groups.get(0);
                        g.names.clear();
                        g.personCount = 15;
                        g.pauses.clear();
                        g.start = Calendar.getInstance();
                        g.start.set(Calendar.DAY_OF_MONTH, 1);
                        currentGroupIndex = 0;
                        loadGroup();
                        renderGroupTabs();
                        saveGroups();
                    }).show();
            return;
        }
        dialog().setTitle(t("delete_group"))
                .setMessage(groups.get(index).title + t("delete_message"))
                .setNegativeButton(t("cancel"), null)
                .setPositiveButton(t("delete"), (d, w) -> {
                    saveCurrentToGroup();
                    groups.remove(index);
                    if (currentGroupIndex > index) currentGroupIndex--;
                    else if (currentGroupIndex >= groups.size()) currentGroupIndex = groups.size() - 1;
                    loadGroup();
                    renderGroupTabs();
                    saveGroups();
                }).show();
    }

    private void switchGroup(int index) {
        if (index == currentGroupIndex) return;
        saveCurrentToGroup();
        currentGroupIndex = index;
        loadGroup();
        renderGroupTabs();
        if (allNamesEntered()) generatePlan();
        else resultBox.removeAllViews();
    }

    private boolean allNamesEntered() {
        for (int i = 0; i < personCount; i++) {
            if (nameFields[i].getText().toString().trim().isEmpty()) return false;
        }
        return true;
    }

    // ------------------------------------------------------------------ Monatsauswahl und Pausen

    private String[] monthNames() {
        String[] de = currentMonths();
        String[] out = new String[12];
        for (int i = 0; i < 12; i++) out[i] = de[i];
        return out;
    }

    private void chooseMonth(Calendar initial, boolean start) {
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        int initYear = initial.get(Calendar.YEAR);

        final NumberPicker month = new NumberPicker(this);
        month.setMinValue(0);
        month.setMaxValue(11);
        month.setDisplayedValues(monthNames());
        month.setValue(initial.get(Calendar.MONTH));
        month.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        final NumberPicker year = new NumberPicker(this);
        year.setMinValue(Math.min(thisYear - 3, initYear));
        year.setMaxValue(Math.max(thisYear + 10, initYear));
        year.setValue(initYear);
        year.setWrapSelectorWheel(false);
        year.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        row.setPadding(dp(16), dp(8), dp(16), 0);
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, -2, 3);
        mp.setMargins(0, 0, dp(12), 0);
        row.addView(month, mp);
        row.addView(year, new LinearLayout.LayoutParams(0, -2, 1));

        dialog().setTitle(start ? t("start") : t("pause_month")).setView(row)
                .setNegativeButton(t("cancel"), null)
                .setPositiveButton(t("apply"), (d, w) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(year.getValue(), month.getValue(), 1);
                    if (start) {
                        selectedStart = c;
                        startButton.setText(formatMonth(c));
                    } else {
                        if (!containsMonth(c)) pauseMonths.add(c);
                        renderPauses();
                    }
                }).show();
    }

    private boolean containsMonth(Calendar c) {
        for (Calendar p : pauseMonths) if (key(p).equals(key(c))) return true;
        return false;
    }

    private void renderPauses() {
        if (pauseBox == null) return;
        pauseBox.removeAllViews();
        Collections.sort(pauseMonths, (a, b) -> a.compareTo(b));
        for (int i = 0; i < pauseMonths.size(); i++) {
            final int at = i;
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(shape(SURFACE, LINE, 10));
            TextView t = text(formatMonth(pauseMonths.get(i)), 15, INK, null);
            t.setPadding(dp(14), 0, 0, 0);
            row.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
            TextView x = text("Entfernen", 13, DANGER, MEDIUM);
            x.setGravity(Gravity.CENTER);
            x.setPadding(dp(14), 0, dp(14), 0);
            x.setOnClickListener(v -> {
                pauseMonths.remove(at);
                renderPauses();
            });
            row.addView(x, new LinearLayout.LayoutParams(-2, -1));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(46));
            lp.setMargins(0, dp(3), 0, dp(3));
            pauseBox.addView(row, lp);
        }
    }

    // ------------------------------------------------------------------ Planlogik

    private void generatePlan() {
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < personCount; i++) {
            String n = nameFields[i].getText().toString().trim();
            if (n.isEmpty()) {
                nameFields[i].requestFocus();
                toast(String.format(Locale.getDefault(), t("all_names"), personCount));
                return;
            }
            names.add(n);
        }
        currentNames = names;
        int per = MAX / personCount;
        currentPlan = new ArrayList<>();
        Calendar c = (Calendar) selectedStart.clone();
        int active = 0, shown = 0;
        while (active < personCount && shown < 120) {
            MonthPlan p = new MonthPlan();
            p.date = (Calendar) c.clone();
            p.pause = containsMonth(c);
            if (!p.pause) {
                p.numbers = new int[personCount][per];
                for (int i = 0; i < personCount; i++)
                    for (int k = 0; k < per; k++)
                        p.numbers[i][k] = ((i * per + k + per * active) % MAX) + 1;
                active++;
            }
            currentPlan.add(p);
            c.add(Calendar.MONTH, 1);
            shown++;
        }
        saveGroups();
        renderResults();
        scroll.post(() -> scroll.smoothScrollTo(0, resultBox.getTop()));
    }

    private void renderResults() {
        resultBox.removeAllViews();
        final String groupTitle = groups.get(currentGroupIndex).title;

        section(resultBox, "Auswertung");
        TextView h = text(groupTitle, 22, INK, SERIF);
        h.setText(groupTitle);
        margin(h, 0, 0, 0, 12);
        resultBox.addView(h);

        LinearLayout actions = new LinearLayout(this);
        TextView img = button("Übersicht als Bild", false);
        img.setTextSize(14);
        img.setOnClickListener(v -> shareBitmap(makeOverviewBitmap(), safeFile(groupTitle) + "-Gesamtübersicht"));
        LinearLayout.LayoutParams a = new LinearLayout.LayoutParams(0, dp(46), 1);
        a.setMargins(0, 0, dp(4), 0);
        actions.addView(img, a);
        TextView pdf = button("Übersicht als PDF", false);
        pdf.setTextSize(14);
        pdf.setOnClickListener(v -> shareOverviewPdf());
        LinearLayout.LayoutParams b = new LinearLayout.LayoutParams(0, dp(46), 1);
        b.setMargins(dp(4), 0, 0, 0);
        actions.addView(pdf, b);
        margin(actions, 0, 0, 0, 14);
        resultBox.addView(actions);

        resultBox.addView(makeOverviewTable());

        for (int person = 0; person < personCount; person++) {
            final int who = person;
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(16), dp(14), dp(16), dp(6));
            card.setBackground(shape(SURFACE, LINE, 14));

            TextView n = text((person + 1) + ". " + currentNames.get(person), 17, INK, MEDIUM);
            n.setPadding(0, 0, 0, dp(8));
            card.addView(n);

            for (int m = 0; m < currentPlan.size(); m++) {
                MonthPlan p = currentPlan.get(m);
                View div = new View(this);
                div.setBackgroundColor(LINE);
                card.addView(div, new LinearLayout.LayoutParams(-1, 1));
                LinearLayout row = new LinearLayout(this);
                row.setPadding(0, dp(9), 0, dp(9));
                TextView lbl = text(monthDual(p.date), 13.5f, MUTED, null);
                TextView val = text(p.pause ? t("pause") : formatNumbers(p, who, false), 14,
                        p.pause ? MUTED : INK, p.pause ? null : MEDIUM);
                val.setGravity(Gravity.END);
                row.addView(lbl, new LinearLayout.LayoutParams(0, -2, 1));
                row.addView(val, new LinearLayout.LayoutParams(-2, -2));
                card.addView(row);
            }

            TextView export = link("Als Bild teilen", ACCENT);
            export.setOnClickListener(v -> shareBitmap(makePersonBitmap(who),
                    safeFile(groupTitle) + "-" + safeFile(currentNames.get(who))));
            LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(-2, -2);
            ep.gravity = Gravity.END;
            card.addView(export, ep);

            margin(card, 0, 0, 0, 10);
            resultBox.addView(card);
        }
    }

    // ------------------------------------------------------------------ Tabelle auf dem Bildschirm

    private View makeOverviewTable() {
        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout table = new LinearLayout(this);
        table.setOrientation(LinearLayout.VERTICAL);
        table.setBackgroundColor(LINE);
        table.setPadding(1, 1, 0, 0);

        int per = MAX / personCount;
        int nameW = dp(100), cellW = dp(56), headerH = dp(76), rowH = dp(Math.max(52, per * 17 + 14));

        LinearLayout header = new LinearLayout(this);
        header.addView(cell("", nameW, headerH, true, false));
        for (MonthPlan m : currentPlan) header.addView(cell(shortMonth(m.date), cellW, headerH, true, false));
        table.addView(header);

        for (int i = 0; i < personCount; i++) {
            LinearLayout row = new LinearLayout(this);
            row.addView(cell(currentNames.get(i), nameW, rowH, true, false));
            for (MonthPlan m : currentPlan)
                row.addView(cell(m.pause ? t("pause") : formatNumbers(m, i, true), cellW, rowH, false, m.pause));
            table.addView(row);
        }
        hs.addView(table);
        margin(hs, 0, 0, 0, 16);
        return hs;
    }

    private TextView cell(String s, int width, int height, boolean head, boolean muted) {
        TextView v = text(s, 13, muted ? MUTED : INK, head ? MEDIUM : null);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(4), dp(4), dp(4), dp(4));
        v.setBackgroundColor(head ? TINT : SURFACE);
        if (head) {
            v.setMaxLines(3);
            v.setEllipsize(TextUtils.TruncateAt.END);
        }
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width, height);
        p.setMargins(0, 0, 1, 1);
        v.setLayoutParams(p);
        return v;
    }

    // ------------------------------------------------------------------ Texte

    private String monthDual(Calendar c) {
        return currentMonths()[c.get(Calendar.MONTH)] + " " + c.get(Calendar.YEAR);
    }

    private String formatMonth(Calendar c) {
        return monthDual(c);
    }

    private String shortMonth(Calendar c) {
        return currentShortMonths()[c.get(Calendar.MONTH)] + "\n" + c.get(Calendar.YEAR);
    }

    private String[] currentMonths() {
        if (language.equals("en")) return EN_MONTHS;
        if (language.equals("tr")) return TR_MONTHS;
        if (language.equals("ar")) return AR_MONTHS;
        return DE_MONTHS;
    }

    private String[] currentShortMonths() {
        if (language.equals("en")) return EN_SHORT_MONTHS;
        if (language.equals("tr")) return TR_SHORT_MONTHS;
        if (language.equals("ar")) return AR_SHORT_MONTHS;
        return DE_SHORT_MONTHS;
    }

    private String key(Calendar c) {
        return String.format(Locale.ROOT, "%04d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1);
    }

    private String formatNumbers(MonthPlan month, int person, boolean multiline) {
        if (month.numbers == null) return t("pause");
        int[] values = month.numbers[person];
        StringBuilder b = new StringBuilder();
        if (multiline) {
            for (int i = 0; i < values.length; i++) {
                if (i > 0) b.append("\n");
                b.append(values[i]);
            }
            return b.toString();
        }
        if (values.length == 1) return String.valueOf(values[0]);
        for (int i = 0; i < values.length; i++) {
            if (i > 0) b.append(values.length == 2 ? conjunction() : ", ");
            b.append(values[i]);
        }
        return b.toString();
    }

    // ------------------------------------------------------------------ Speichern und Laden

    private void saveCurrentToGroup() {
        if (groups.isEmpty()) return;
        GroupData g = groups.get(currentGroupIndex);
        g.personCount = personCount;
        g.names.clear();
        // "|" ist das Trennzeichen beim Speichern und darf in Namen nicht vorkommen
        for (int i = 0; i < MAX; i++) g.names.add(nameFields[i].getText().toString().replace("|", "/"));
        while (!g.names.isEmpty() && g.names.get(g.names.size() - 1).trim().isEmpty())
            g.names.remove(g.names.size() - 1);
        g.start = (Calendar) selectedStart.clone();
        g.pauses.clear();
        for (Calendar c : pauseMonths) g.pauses.add((Calendar) c.clone());
    }

    private void loadGroup() {
        GroupData g = groups.get(currentGroupIndex);
        personCount = isAllowedCount(g.personCount) ? g.personCount : 15;
        for (int i = 0; i < MAX; i++) nameFields[i].setText(i < g.names.size() ? g.names.get(i) : "");
        updateCountSelector();
        renderNameFields();
        selectedStart = (Calendar) g.start.clone();
        selectedStart.set(Calendar.DAY_OF_MONTH, 1);
        pauseMonths.clear();
        for (Calendar c : g.pauses) pauseMonths.add((Calendar) c.clone());
        startButton.setText(formatMonth(selectedStart));
        renderPauses();
        resultBox.removeAllViews();
    }

    private boolean isAllowedCount(int n) {
        for (int c : COUNTS) if (c == n) return true;
        return false;
    }

    private void saveGroups() {
        saveCurrentToGroup();
        android.content.SharedPreferences.Editor e = getPreferences(0).edit().clear()
                .putString("language", language)
                .putInt("group_count", groups.size());
        for (int i = 0; i < groups.size(); i++) {
            GroupData g = groups.get(i);
            e.putString("group_title_" + i, g.title)
                    .putInt("group_people_" + i, g.personCount)
                    .putString("group_names_" + i, String.join("|", g.names))
                    .putString("group_start_" + i, key(g.start))
                    .putString("group_pauses_" + i, pauseKeys(g.pauses));
        }
        e.apply();
    }

    private String pauseKeys(ArrayList<Calendar> list) {
        StringBuilder b = new StringBuilder();
        for (Calendar c : list) {
            if (b.length() > 0) b.append(",");
            b.append(key(c));
        }
        return b.toString();
    }

    private void loadGroups() {
        android.content.SharedPreferences p = getPreferences(0);
        int count = p.getInt("group_count", 0);
        if (count > 0) {
            groups.clear();
            for (int i = 0; i < count; i++) {
                GroupData g = new GroupData(p.getString("group_title_" + i, "Kuran Hatim " + (i + 1)));
                g.personCount = p.getInt("group_people_" + i, 15);
                String n = p.getString("group_names_" + i, "");
                if (!n.isEmpty()) g.names.addAll(Arrays.asList(n.split("\\|", -1)));
                Calendar s = parseKey(p.getString("group_start_" + i, ""));
                if (s != null) g.start = s;
                String ps = p.getString("group_pauses_" + i, "");
                if (!ps.isEmpty()) for (String k : ps.split(",")) {
                    Calendar c = parseKey(k);
                    if (c != null) g.pauses.add(c);
                }
                groups.add(g);
            }
        } else {
            GroupData g = groups.get(0);
            String n = p.getString("names", "");
            if (n.isEmpty()) for (int i = 0; i < 15; i++) g.names.add(p.getString("name_" + i, ""));
            else g.names.addAll(Arrays.asList(n.split("\\|", -1)));
            Calendar s = parseKey(p.getString("start", ""));
            if (s != null) g.start = s;
            String ps = p.getString("pauses", "");
            if (!ps.isEmpty()) for (String k : ps.split(",")) {
                Calendar c = parseKey(k);
                if (c != null) g.pauses.add(c);
            }
        }
        migrateLegacyTitles();
        currentGroupIndex = 0;
        loadGroup();
        renderGroupTabs();
        if (allNamesEntered()) generatePlan();
    }

    private Calendar parseKey(String s) {
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM", Locale.ROOT);
            f.setLenient(false);
            Date d = f.parse(s);
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            c.set(Calendar.DAY_OF_MONTH, 1);
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ Export als Bild und PDF

    // Schrift so verkleinern, dass der Text in die verfügbare Breite passt
    private void fit(Paint p, String s, float maxW, float size) {
        p.setTextSize(size);
        float w = p.measureText(s);
        if (w > maxW) p.setTextSize(Math.max(18f, size * maxW / w));
    }

    private void center(Canvas c, String s, float x, float y, Paint p) {
        Paint.FontMetrics f = p.getFontMetrics();
        c.drawText(s, x - p.measureText(s) / 2f, y - (f.ascent + f.descent) / 2f, p);
    }

    private void drawGrid(Canvas c, int left, int top, int nameRight, int right, int bottom,
                          int columns, int headerH, int rowH, int rows) {
        Paint fill = new Paint();
        fill.setColor(TINT);
        c.drawRect(left, top, right, top + headerH, fill);
        fill.setColor(0xFFF3F6F2);
        c.drawRect(left, top + headerH, nameRight, bottom, fill);

        Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        line.setColor(0xFFC9C5B8);
        line.setStrokeWidth(3);
        c.drawLine(left, top, left, bottom, line);
        c.drawLine(nameRight, top, nameRight, bottom, line);
        int cellWidth = (right - nameRight) / Math.max(1, columns);
        for (int i = 1; i <= columns; i++) c.drawLine(nameRight + cellWidth * i, top, nameRight + cellWidth * i, bottom, line);
        c.drawLine(left, top, right, top, line);
        c.drawLine(left, top + headerH, right, top + headerH, line);
        for (int i = 1; i <= rows; i++) c.drawLine(left, top + headerH + rowH * i, right, top + headerH + rowH * i, line);
    }

    private Bitmap makePersonBitmap(int person) {
        int w = 2480, h = 3508, left = 180, right = 2300, top = 380, headerH = 150, mid = left + 1100;
        int rows = currentPlan.size();
        int rowH = Math.min(165, (h - top - headerH - 160) / Math.max(1, rows));
        int bottom = top + headerH + rowH * rows;

        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.drawColor(Color.WHITE);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        p.setTypeface(SERIF_BOLD);
        p.setColor(INK);
        String name = currentNames.get(person);
        fit(p, name, right - left, 100);
        c.drawText(name, left, 170, p);
        p.setTypeface(MEDIUM);
        p.setColor(MUTED);
        p.setTextSize(48);
        c.drawText(groups.get(currentGroupIndex).title, left, 260, p);

        drawGrid(c, left, top, mid, right, bottom, 1, headerH, rowH, rows);

        p.setColor(INK);
        p.setTypeface(MEDIUM);
        p.setTextSize(52);
        center(c, t("month_header"), (left + mid) / 2f, top + headerH / 2f, p);
        center(c, t("cuz_header"), (mid + right) / 2f, top + headerH / 2f, p);

        float ts = Math.min(52f, rowH * .36f);
        for (int i = 0; i < rows; i++) {
            MonthPlan m = currentPlan.get(i);
            float cy = top + headerH + rowH * i + rowH / 2f;
            String month = monthDual(m.date);
            p.setColor(INK);
            fit(p, month, mid - left - 40, ts);
            center(c, month, (left + mid) / 2f, cy, p);
            String value = m.pause ? t("pause") : formatNumbers(m, person, false);
            p.setColor(m.pause ? MUTED : INK);
            fit(p, value, right - mid - 40, ts * 1.1f);
            center(c, value, (mid + right) / 2f, cy, p);
        }
        return b;
    }

    private Bitmap makeOverviewBitmap() {
        int w = 3508, h = 2480, left = 80, top = 170, nameW = 650, header = 540;
        int cols = currentPlan.size();
        int cellW = (w - left - nameW - 80) / Math.max(1, cols);
        int right = left + nameW + cellW * cols;
        int rowH = Math.min(118, (h - top - header - 50) / Math.max(1, personCount));
        int bottom = top + header + rowH * personCount;

        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.drawColor(Color.WHITE);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        String title = groups.get(currentGroupIndex).title;
        p.setTypeface(SERIF_BOLD);
        p.setColor(INK);
        fit(p, title, w - 2 * left, 80);
        c.drawText(title, left, 115, p);

        drawGrid(c, left, top, left + nameW, right, bottom, cols, header, rowH, personCount);

        p.setTypeface(MEDIUM);
        p.setColor(INK);
        for (int j = 0; j < cols; j++) {
            float x = left + nameW + j * cellW + cellW / 2f;
            String month = monthDual(currentPlan.get(j).date);
            fit(p, month, header - 40, Math.min(44f, cellW * .5f));
            c.save();
            c.rotate(-90, x, top + header / 2f);
            center(c, month, x, top + header / 2f, p);
            c.restore();
        }

        for (int i = 0; i < personCount; i++) {
            float cy = top + header + i * rowH + rowH / 2f;
            p.setColor(INK);
            p.setTypeface(MEDIUM);
            String name = (i + 1) + ". " + currentNames.get(i);
            fit(p, name, nameW - 40, Math.min(56f, rowH * .5f));
            Paint.FontMetrics fm = p.getFontMetrics();
            c.drawText(name, left + 20, cy - (fm.ascent + fm.descent) / 2f, p);

            for (int j = 0; j < cols; j++) {
                MonthPlan m = currentPlan.get(j);
                float x = left + nameW + j * cellW + cellW / 2f;
                if (m.pause) {
                    p.setColor(MUTED);
                    p.setTypeface(Typeface.DEFAULT);
                    fit(p, t("pause"), cellW - 12, 30);
                    center(c, t("pause"), x, cy, p);
                } else if (m.numbers[i].length == 2) {
                    p.setColor(INK);
                    p.setTypeface(MEDIUM);
                    String first = m.numbers[i][0] + conjunction().trim();
                    String second = String.valueOf(m.numbers[i][1]);
                    float s = Math.min(44f, rowH * .36f);
                    fit(p, first, cellW - 12, s);
                    center(c, first, x, cy - s * .55f, p);
                    center(c, second, x, cy + s * .55f, p);
                } else {
                    p.setColor(INK);
                    p.setTypeface(MEDIUM);
                    String value = formatNumbers(m, i, false);
                    fit(p, value, cellW - 12, Math.min(40f, rowH * .5f));
                    center(c, value, x, cy, p);
                }
            }
        }
        return b;
    }

    private void shareBitmap(Bitmap bitmap, String title) {
        try {
            String fn = safeFile(title) + ".png";
            ContentValues v = new ContentValues();
            v.put(MediaStore.Images.Media.DISPLAY_NAME, fn);
            v.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            if (Build.VERSION.SDK_INT >= 29) v.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/KuranHatim");
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
            if (uri == null) throw new Exception();
            try (java.io.OutputStream out = getContentResolver().openOutputStream(uri)) {
                if (out == null) throw new Exception();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("image/png");
            i.putExtra(Intent.EXTRA_STREAM, uri);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(i, t("share_image_chooser")));
        } catch (Exception e) {
            toast("Bild konnte nicht gespeichert werden.");
        }
    }

    private void shareOverviewPdf() {
        PdfDocument document = new PdfDocument();
        try {
            Bitmap bitmap = makeOverviewBitmap();
            PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(842, 595, 1).create();
            PdfDocument.Page page = document.startPage(info);
            page.getCanvas().drawBitmap(bitmap, null, new Rect(0, 0, 842, 595),
                    new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            document.finishPage(page);
            String fn = safeFile(groups.get(currentGroupIndex).title) + "-Gesamtübersicht.pdf";
            java.io.File file = new java.io.File(getCacheDir(), fn);
            try (java.io.OutputStream out = new java.io.FileOutputStream(file)) {
                document.writeTo(out);
            }
            Uri uri = Uri.parse("content://ry.dev.kuranhatimplanner.pdfprovider/" + Uri.encode(fn));
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/pdf");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, t("share_pdf_chooser")));
        } catch (Exception e) {
            toast("PDF konnte nicht erstellt werden.");
        } finally {
            document.close();
        }
    }

    private String safeFile(String s) {
        return s.replaceAll("[^a-zA-Z0-9ÄÖÜäöüß_-]", "_");
    }

    // ------------------------------------------------------------------ Datenklassen

    private static class MonthPlan {
        Calendar date;
        boolean pause;
        int[][] numbers;
    }

    private static class GroupData {
        String title;
        int personCount = 15;
        ArrayList<String> names = new ArrayList<>();
        Calendar start;
        ArrayList<Calendar> pauses = new ArrayList<>();

        GroupData(String t) {
            title = t;
            start = Calendar.getInstance();
            start.set(Calendar.DAY_OF_MONTH, 1);
        }
    }

    // Schlichtes Menüsymbol aus drei Linien, damit keine Icon-Datei nötig ist
    private static class MenuIcon extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        MenuIcon(android.content.Context context, int color, float strokeWidth) {
            super(context);
            paint.setColor(color);
            paint.setStrokeWidth(strokeWidth);
            paint.setStrokeCap(Paint.Cap.ROUND);
        }

        @Override
        protected void onDraw(Canvas c) {
            float w = getWidth(), h = getHeight();
            float x0 = w * .1f, x1 = w * .9f;
            c.drawLine(x0, h * .25f, x1, h * .25f, paint);
            c.drawLine(x0, h * .5f, x1, h * .5f, paint);
            c.drawLine(x0, h * .75f, x1, h * .75f, paint);
        }
    }
}
