package com.osmehdi.talabini;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends Activity {

    private EditText nameInput;
    private LinearLayout results;

    private final int BG = Color.rgb(244, 241, 234);
    private final int CARD = Color.WHITE;
    private final int TEXT = Color.rgb(45, 40, 36);
    private final int ACCENT = Color.rgb(109, 76, 65);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(24), dp(18), dp(30));
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("🔮 طالع‌بینی اوس مهدی");
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextColor(TEXT);
        title.setGravity(Gravity.CENTER);
        root.addView(title, lp(-1, -2, 0, 0, 0, 14));

        TextView sub = new TextView(this);
        sub.setText("اسمتو وارد کن تا ببینیم سرنوشت چی برات نوشته 😄");
        sub.setTextSize(15);
        sub.setTextColor(Color.DKGRAY);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub, lp(-1, -2, 0, 0, 0, 18));

        nameInput = new EditText(this);
        nameInput.setHint("نام");
        nameInput.setTextSize(18);
        nameInput.setSingleLine(true);
        nameInput.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        nameInput.setPadding(dp(16), 0, dp(16), 0);
        nameInput.setBackgroundColor(Color.WHITE);
        root.addView(nameInput, lp(-1, dp(56), 0, 0, 0, 12));

        Button fortuneButton = new Button(this);
        fortuneButton.setText("طالع منو بگیر");
        fortuneButton.setTextSize(17);
        fortuneButton.setTextColor(Color.WHITE);
        fortuneButton.setBackgroundColor(ACCENT);
        root.addView(fortuneButton, lp(-1, dp(56), 0, 0, 0, 20));

        results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        root.addView(results, lp(-1, -2, 0, 0, 0, 0));

        fortuneButton.setOnClickListener(v -> generate());

        scroll.addView(root);
        setContentView(scroll);
    }

    private void generate() {
        String name = nameInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError("اول اسمتو وارد کن");
            return;
        }

        results.removeAllViews();

        addResult("👤 نام", name);
        addResult("📝 توصیف", randomFromAsset("description.txt"));
        addResult("🔮 سرنوشت", randomFromAsset("fate.txt"));

        // ثروت یک بار تولید می‌شود و همان مقدار
        // برای تعیین بدترین و بهترین مرگ استفاده می‌شود.
        long wealth = randomWealth();

        DeathResult death = generateDeathResult(wealth);

        addResult("🌤️ بهترین نوع مرگ", death.bestText);
        addResult("🌑 بدترین نوع مرگ", death.worstText);
        addResult("💬 نصیحت اوس مهدی", randomFromAsset("advice.txt"));
        addResult("💰 ثروت نهایی", formatToman(wealth));
    }

    private String randomFromAsset(String filename) {
        List<String> options = new ArrayList<>();

        try (InputStream is = getAssets().open(filename);
             BufferedReader br = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder all = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                if (all.length() > 0) {
                    all.append('\n');
                }
                all.append(line);
            }

            String[] parts = all.toString().split(",");

            for (String part : parts) {
                String value = part.trim();

                if (!value.isEmpty()) {
                    options.add(value);
                }
            }

        } catch (Exception e) {
            return "برای این بخش هنوز چیزی ننوشتی!";
        }

        if (options.isEmpty()) {
            return "برای این بخش هنوز چیزی ننوشتی!";
        }

        return options.get(
                ThreadLocalRandom.current().nextInt(options.size())
        );
    }

    /*
     * ============================================================
     * منطق مرگ
     * ============================================================
     *
     * death.txt به شکل زیر است:
     *
     * 0|متن مرگ اول,
     * 1|متن مرگ دوم,
     * 1|متن مرگ سوم,
     * 5|متن مرگ چهارم
     *
     * جداکننده واقعی گزینه‌ها فقط کاما است.
     * Enter فقط برای خوانایی فایل است و نادیده گرفته می‌شود.
     *
     * هر گزینه دارای:
     *
     * score|text
     *
     * است.
     */

    private DeathResult generateDeathResult(long wealth) {

        List<DeathOption> deaths = loadDeaths();

        if (deaths.isEmpty()) {
            return new DeathResult(
                    "برای بخش مرگ هنوز چیزی ننوشتی!",
                    "برای بخش مرگ هنوز چیزی ننوشتی!"
            );
        }

        int worstScore = randomWorstScore(wealth);

        /*
         * اگر به هر دلیلی death.txt برای امتیاز انتخاب‌شده
         * متنی نداشته باشد، نزدیک‌ترین امتیاز موجود را پیدا می‌کنیم.
         */
        int actualWorstScore = findAvailableScore(
                deaths,
                worstScore,
                false
        );

        /*
         * بهترین مرگ حتماً از worstScore تا 10 است.
         */
        int bestScore = randomIntInclusive(actualWorstScore, 10);

        /*
         * اگر برای bestScore متن وجود نداشته باشد،
         * نزدیک‌ترین امتیاز موجود که >= worstScore باشد انتخاب می‌شود.
         */
        int actualBestScore = findAvailableScore(
                deaths,
                bestScore,
                true
        );

        String worstText = randomDeathText(deaths, actualWorstScore);
        String bestText = randomDeathText(deaths, actualBestScore);

        return new DeathResult(bestText, worstText);
    }

    /*
     * فایل death.txt را کامل می‌خواند.
     *
     * \r و \n حذف می‌شوند، ولی فاصله‌های داخل متن‌ها حفظ می‌شوند.
     * بنابراین:
     *
     * 0|متن اول,
     * 1|متن دوم,
     *
     * از نظر برنامه معادل:
     *
     * 0|متن اول,1|متن دوم,
     */
    private List<DeathOption> loadDeaths() {

        List<DeathOption> deaths = new ArrayList<>();

        try (InputStream is = getAssets().open("death.txt");
             BufferedReader br = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder all = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                all.append(line);
            }

            String[] entries = all.toString().split(",");

            for (String entry : entries) {

                String value = entry.trim();

                if (value.isEmpty()) {
                    continue;
                }

                int separator = value.indexOf('|');

                if (separator <= 0 || separator >= value.length() - 1) {
                    continue;
                }

                String scoreText = value.substring(0, separator).trim();
                String deathText = value.substring(separator + 1).trim();

                try {
                    int score = Integer.parseInt(scoreText);

                    if (score < 0 || score > 10) {
                        continue;
                    }

                    if (deathText.isEmpty()) {
                        continue;
                    }

                    deaths.add(new DeathOption(score, deathText));

                } catch (NumberFormatException ignored) {
                    // ورودی نامعتبر نادیده گرفته می‌شود.
                }
            }

        } catch (Exception e) {
            return deaths;
        }

        return deaths;
    }

    /*
     * تعیین بازه worstScore بر اساس ثروت.
     *
     * 0 تومان                  -> 0
     * 1K تا 100M               -> 0..2
     * 100M تا 1B               -> 0..3
     * 1B تا 10B                -> 1..5
     * 10B تا 100B              -> 2..6
     * 100B تا 1T               -> 3..7
     * 1T تا 10T                -> 4..8
     * 10T تا 100T              -> 5..9
     *
     * اگر ثروت در مرز مشترک باشد،
     * بازه بالاتر انتخاب می‌شود.
     */
    private int randomWorstScore(long wealth) {

        if (wealth == 0L) {
            return 0;
        }

        int min;
        int max;

        if (wealth < 100_000_000L) {
            // 1 هزار تا کمتر از 100 میلیون
            min = 0;
            max = 2;

        } else if (wealth < 1_000_000_000L) {
            // 100 میلیون تا کمتر از 1 میلیارد
            min = 0;
            max = 3;

        } else if (wealth < 10_000_000_000L) {
            // 1 میلیارد تا کمتر از 10 میلیارد
            min = 1;
            max = 5;

        } else if (wealth < 100_000_000_000L) {
            // 10 میلیارد تا کمتر از 100 میلیارد
            min = 2;
            max = 6;

        } else if (wealth < 1_000_000_000_000L) {
            // 100 میلیارد تا کمتر از 1 تریلیون
            min = 3;
            max = 7;

        } else if (wealth < 10_000_000_000_000L) {
            // 1 تریلیون تا کمتر از 10 تریلیون
            min = 4;
            max = 8;

        } else {
            // 10 تریلیون تا 100 تریلیون
            min = 5;
            max = 9;
        }

        return randomIntInclusive(min, max);
    }

    /*
     * اگر چند متن یک امتیاز داشته باشند،
     * یکی از خود متن‌ها نیز تصادفی انتخاب می‌شود.
     */
    private String randomDeathText(
            List<DeathOption> deaths,
            int score
    ) {

        List<String> matching = new ArrayList<>();

        for (DeathOption death : deaths) {
            if (death.score == score) {
                matching.add(death.text);
            }
        }

        if (matching.isEmpty()) {
            return "برای این امتیاز متن مرگی پیدا نشد!";
        }

        return matching.get(
                ThreadLocalRandom.current().nextInt(matching.size())
        );
    }

    /*
     * worstScore باید <= bestScore باشد.
     *
     * این متد امتیاز موجود را پیدا می‌کند.
     *
     * برای worst:
     * اگر امتیاز دقیقاً وجود نداشت، نزدیک‌ترین امتیاز موجود
     * انتخاب می‌شود.
     *
     * برای best:
     * ترجیح با امتیاز موجود بزرگ‌تر یا مساوی است.
     */
    private int findAvailableScore(
            List<DeathOption> deaths,
            int requestedScore,
            boolean best
    ) {

        boolean[] available = new boolean[11];

        for (DeathOption death : deaths) {
            if (death.score >= 0 && death.score <= 10) {
                available[death.score] = true;
            }
        }

        if (available[requestedScore]) {
            return requestedScore;
        }

        if (best) {

            for (int score = requestedScore + 1; score <= 10; score++) {
                if (available[score]) {
                    return score;
                }
            }

            for (int score = requestedScore - 1; score >= 0; score--) {
                if (available[score]) {
                    return score;
                }
            }

        } else {

            for (int score = requestedScore - 1; score >= 0; score--) {
                if (available[score]) {
                    return score;
                }
            }

            for (int score = requestedScore + 1; score <= 10; score++) {
                if (available[score]) {
                    return score;
                }
            }
        }

        return requestedScore;
    }

    /*
     * عدد تصادفی شامل min و max.
     */
    private int randomIntInclusive(int min, int max) {
        if (min >= max) {
            return min;
        }

        return ThreadLocalRandom.current().nextInt(
                min,
                max + 1
        );
    }

    /*
     * ============================================================
     * منطق ثروت
     * ============================================================
     *
     * 3.5٪ دقیقاً صفر تومان
     * 66.5٪ بین 1 هزار تا 1 میلیارد
     * 20٪ بین 1 تا 10 میلیارد
     * 7٪ بین 10 تا 100 میلیارد
     * 2٪ بین 100 میلیارد تا 1 تریلیون
     * 1٪ بین 1 تا 100 تریلیون
     *
     * همه اعداد مضرب 1000 تومان هستند.
     */
    private long randomWealth() {

        long roll = ThreadLocalRandom.current().nextLong(1000);

        // 3.5٪ دقیقاً صفر تومان
        if (roll < 35) {
            return 0L;
        }

        // 66.5٪ بین 1 هزار تا 1 میلیارد تومان
        if (roll < 700) {
            return randomAmount(
                    1_000L,
                    1_000_000_000L
            );
        }

        // 20٪ بین 1 تا 10 میلیارد تومان
        if (roll < 900) {
            return randomAmount(
                    1_000_000_000L,
                    10_000_000_000L
            );
        }

        // 7٪ بین 10 تا 100 میلیارد تومان
        if (roll < 970) {
            return randomAmount(
                    10_000_000_000L,
                    100_000_000_000L
            );
        }

        // 2٪ بین 100 میلیارد تا 1 تریلیون تومان
        if (roll < 990) {
            return randomAmount(
                    100_000_000_000L,
                    1_000_000_000_000L
            );
        }

        // 1٪ بین 1 تا 100 تریلیون تومان
        return randomAmount(
                1_000_000_000_000L,
                100_000_000_000_000L
        );
    }

    private long randomAmount(long min, long max) {

        final long STEP = 1_000L;

        long minIndex = min / STEP;
        long maxIndex = max / STEP;

        return ThreadLocalRandom.current().nextLong(
                minIndex,
                maxIndex + 1
        ) * STEP;
    }

    private String formatToman(long amount) {

        String s = String.format(
                java.util.Locale.US,
                "%,d",
                amount
        );

        return toPersianDigits(s) + " تومان";
    }

    private String toPersianDigits(String s) {

        return s
                .replace('0', '۰')
                .replace('1', '۱')
                .replace('2', '۲')
                .replace('3', '۳')
                .replace('4', '۴')
                .replace('5', '۵')
                .replace('6', '۶')
                .replace('7', '۷')
                .replace('8', '۸')
                .replace('9', '۹');
    }

    private void addResult(String title, String value) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(14)
        );
        card.setBackgroundColor(CARD);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(15);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setTextColor(ACCENT);
        t.setGravity(Gravity.RIGHT);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(18);
        v.setTextColor(TEXT);
        v.setGravity(Gravity.RIGHT);
        v.setPadding(0, dp(7), 0, 0);

        card.addView(t);
        card.addView(v);

        results.addView(
                card,
                lp(-1, -2, 0, 0, 0, 8)
        );
    }

    private LinearLayout.LayoutParams lp(
            int w,
            int h,
            int l,
            int t,
            int r,
            int b
    ) {

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(w, h);

        p.setMargins(
                dp(l),
                dp(t),
                dp(r),
                dp(b)
        );

        return p;
    }

    private int dp(int value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    /*
     * یک گزینه مرگ:
     *
     * score = امتیاز 0 تا 10
     * text  = متن مرگ
     */
    private static class DeathOption {

        final int score;
        final String text;

        DeathOption(int score, String text) {
            this.score = score;
            this.text = text;
        }
    }

    /*
     * نتیجه نهایی سیستم مرگ.
     */
    private static class DeathResult {

        final String bestText;
        final String worstText;

        DeathResult(
                String bestText,
                String worstText
        ) {
            this.bestText = bestText;
            this.worstText = worstText;
        }
    }
}
