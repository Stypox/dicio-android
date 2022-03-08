package org.dicio.dicio_android.skills.timer;

import static org.dicio.dicio_android.Sections.getSection;
import static org.dicio.dicio_android.SectionsGenerated.util_yes_no;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.dicio_android.util.StringUtils;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillContext;
import org.dicio.skill.SkillInfo;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.chain.InputRecognizer;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.standard.StandardRecognizer;
import org.dicio.skill.standard.StandardResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// TODO cleanup this skill and use a service to manage timers
public class TimerOutput extends OutputGenerator<TimerOutput.Data> {

    public enum Action {
        set, cancel, query
    }

    public static class Data {
        public Action action;
        @Nullable public Duration duration = null;
        @Nullable public String name = null;
    }

    private static class SetTimer {
        final String name;
        final CountDownTimer countDownTimer;
        final Consumer<String> onCancelCallback;
        long lastTickSeconds;

        public SetTimer(@NonNull final Duration duration,
                        @Nullable final String name,
                        final BiConsumer<String, Long> onTickCallback,
                        final Consumer<String> onFinishCallback,
                        final Consumer<String> onCancelCallback) {
            if (name == null) {
                this.name = null;
            } else {
                this.name = name.trim();
            }
            this.onCancelCallback = onCancelCallback;

            lastTickSeconds = duration.getSeconds();
            this.countDownTimer = new CountDownTimer(duration.toMillis(), 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    lastTickSeconds = millisUntilFinished / 1000;
                    onTickCallback.accept(name, lastTickSeconds);
                }

                @Override
                public void onFinish() {
                    onFinishCallback.accept(name);
                    // removed from setTimers list automatically here
                    setTimers.removeIf(setTimer -> setTimer.countDownTimer == this);
                }
            };
            this.countDownTimer.start();
        }

        public void cancel() {
            // removed from setTimers list by caller
            countDownTimer.cancel();
            onCancelCallback.accept(name);
        }
    }

    private static final List<SetTimer> setTimers = new ArrayList<>();

    private Data askForDurationData = null;
    private boolean confirmCancelAll = false;


    public TimerOutput(final SkillContext context, @Nullable final SkillInfo skillInfo) {
        super(context, skillInfo);
    }

    @Override
    public void generate(final Data data) {
        switch (data.action) {
            case set:
                if (data.duration == null) {
                    final String message = ctx().android().getString(R.string.skill_timer_how_much_time);
                    ctx().getSpeechOutputDevice().speak(message);
                    ctx().getGraphicalOutputDevice().display(
                            GraphicalOutputUtils.buildSubHeader(ctx().android(), message));

                    askForDurationData = data;
                    return;
                }
                setTimer(data.duration, data.name);
                break;

            case cancel:
                if (data.name == null && setTimers.size() > 1) {
                    final String message = ctx().android()
                            .getString(R.string.skill_timer_confirm_cancel);
                    ctx().getSpeechOutputDevice().speak(message);
                    ctx().getGraphicalOutputDevice().display(
                            GraphicalOutputUtils.buildSubHeader(ctx().android(), message));

                    confirmCancelAll = true;
                    return;
                }
                cancelTimer(data.name);
                break;

            case query:
                queryTimer(data.name);
                break;
        }
    }

    @Override
    public List<Skill> nextSkills() {
        if (askForDurationData != null) {
            // use local variable, otherwise cleaned up by cleanup()
            final String askForDurationDataName = askForDurationData.name;
            return Collections.singletonList(new Skill(null, null) {
                private String input;
                private Duration duration;

                @Override
                public InputRecognizer.Specificity specificity() {
                    return InputRecognizer.Specificity.high;
                }

                @Override
                public void setInput(final String input,
                                     final List<String> inputWords,
                                     final List<String> normalizedWordKeys) {
                    this.input = input;
                }

                @Override
                public float score() {
                    duration = ctx().requireNumberParserFormatter()
                            .extractDuration(input).get();
                    return duration == null ? 0.0f : 1.0f;
                }

                @Override
                public void processInput() {
                }

                @Override
                public void generateOutput() {
                    if (duration != null) {
                        setTimer(duration, askForDurationDataName);
                    }
                }

                @Override
                public void cleanup() {
                    input = null;
                    duration = null;
                }
            });

        } else if (confirmCancelAll) {
            return Collections.singletonList(new ChainSkill.Builder(ctx(), null)
                    .recognize(new StandardRecognizer(ctx(), null, getSection(util_yes_no)))
                    .output(new OutputGenerator<StandardResult>(ctx(), null) {
                        @Override
                        public void generate(final StandardResult data) {
                            if ("yes".equals(data.getSentenceId())) {
                                cancelTimer(null);
                            } else {
                                final String message = ctx().android()
                                        .getString(R.string.skill_timer_none_canceled);
                                ctx().getSpeechOutputDevice().speak(message);
                                ctx().getGraphicalOutputDevice().display(GraphicalOutputUtils
                                        .buildSubHeader(ctx().android(), message));
                            }
                        }

                        @Override
                        public void cleanup() {}
                    }));

        } else {
            return super.nextSkills();
        }
    }

    @Override
    public void cleanup() {
        askForDurationData = null;
        confirmCancelAll = false;
    }

    private void setTimer(@NonNull final Duration duration,
                          @Nullable final String name) {
        ctx().getSpeechOutputDevice().speak(formatStringWithName(name, duration.getSeconds(),
                R.string.skill_timer_set, R.string.skill_timer_set_name));

        final TextView textView = GraphicalOutputUtils.buildSubHeader(ctx().android(),
                getFormattedDuration(duration.getSeconds(), false));
        ctx().getGraphicalOutputDevice().display(textView);

        setTimers.add(new SetTimer(duration, name,
                (theName, seconds) -> {
                    textView.setText(getFormattedDuration(seconds, false));
                    if (seconds <= 5) {
                        ctx().getSpeechOutputDevice().speak(ctx()
                                .requireNumberParserFormatter().pronounceNumber(seconds).get());
                    }
                },
                (theName) -> {
                    // TODO improve how alarm is played, and allow stopping it
                    final String message = formatStringWithName(theName,
                            R.string.skill_timer_expired, R.string.skill_timer_expired_name);
                    final Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(
                            ctx().android(), RingtoneManager.TYPE_ALARM);
                    final Ringtone ringtone;
                    if (ringtoneUri == null) {
                        ringtone = null;
                    } else {
                        ringtone = RingtoneManager.getRingtone(ctx().android(),
                                ringtoneUri);
                    }

                    if (ringtone == null) {
                        ctx().getSpeechOutputDevice().speak(message);
                    } else {
                        ringtone.play();
                    }
                    textView.setText(message);
                },
                (theName) -> textView.setText(formatStringWithName(theName,
                        R.string.skill_timer_canceled, R.string.skill_timer_canceled_name))));
    }

    private void cancelTimer(@Nullable final String name) {
        final String message;
        if (setTimers.isEmpty()) {
            message = ctx().android()
                    .getString(R.string.skill_timer_no_active);

        } else if (name == null) {
            if (setTimers.size() == 1) {
                if (setTimers.get(0).name == null) {
                    message = ctx().android().getString(R.string.skill_timer_canceled);
                } else {
                    message = ctx().android()
                            .getString(R.string.skill_timer_canceled_name, setTimers.get(0).name);
                }
            } else {
                message = ctx().android().getString(R.string.skill_timer_all_canceled);
            }

            // cancel all
            for (final SetTimer setTimer : setTimers) {
                setTimer.cancel();
            }
            setTimers.clear();

        } else {
            final SetTimer setTimer = getSetTimerWithSimilarName(name);
            if (setTimer == null) {
                message = ctx().android()
                        .getString(R.string.skill_timer_no_active_name, name);

            } else {
                message = ctx().android()
                        .getString(R.string.skill_timer_canceled_name, setTimer.name);

                setTimer.cancel();
                setTimers.remove(setTimer);
            }
        }

        ctx().getSpeechOutputDevice().speak(message);
        ctx().getGraphicalOutputDevice().display(
                GraphicalOutputUtils.buildSubHeader(ctx().android(), message));
    }

    private void queryTimer(@Nullable final String name) {
        final String message;
        if (setTimers.isEmpty()) {
            message = ctx().android()
                    .getString(R.string.skill_timer_no_active);

        } else if (name == null) {
            // no name provided by the user: query the last timer, but adapt the message if only one
            final SetTimer lastTimer = setTimers.get(setTimers.size() - 1);
            @StringRes final int noNameQueryString = setTimers.size() == 1
                    ? R.string.skill_timer_query : R.string.skill_timer_query_last;

            message = formatStringWithName(lastTimer.name, lastTimer.lastTickSeconds,
                    noNameQueryString, R.string.skill_timer_query_name);

        } else {
            final SetTimer setTimer = getSetTimerWithSimilarName(name);
            if (setTimer == null) {
                message = ctx().android().getString(R.string.skill_timer_no_active_name, name);
            } else {
                message = ctx().android().getString(R.string.skill_timer_query_name, setTimer.name,
                                getFormattedDuration(setTimer.lastTickSeconds, true));
            }
        }

        ctx().getSpeechOutputDevice().speak(message);
        ctx().getGraphicalOutputDevice().display(
                GraphicalOutputUtils.buildSubHeader(ctx().android(), message));
    }

    private String getFormattedDuration(final long seconds,
                                        final boolean speech) {
        return ctx().requireNumberParserFormatter()
                .niceDuration(Duration.ofSeconds(seconds))
                .speech(speech)
                .get();
    }

    private String formatStringWithName(@Nullable final String name,
                                        @StringRes final int stringWithoutName,
                                        @StringRes final int stringWithName) {
        if (name == null) {
            return ctx().android().getString(stringWithoutName);
        } else {
            return ctx().android().getString(stringWithName, name);
        }
    }

    private String formatStringWithName(@Nullable final String name,
                                        final long seconds,
                                        @StringRes final int stringWithoutName,
                                        @StringRes final int stringWithName) {
        final String duration = getFormattedDuration(seconds, true);
        if (name == null) {
            return ctx().android().getString(stringWithoutName, duration);
        } else {
            return ctx().android().getString(stringWithName, name, duration);
        }
    }

    private SetTimer getSetTimerWithSimilarName(@NonNull final String name) {
        class Pair {
            final SetTimer setTimer;
            final int distance;

            Pair(final SetTimer setTimer, final int distance) {
                this.setTimer = setTimer;
                this.distance = distance;
            }
        }

        return setTimers.stream()
                .filter(setTimer -> setTimer.name != null)
                .map(setTimer -> new Pair(setTimer,
                        StringUtils.customStringDistance(name, setTimer.name)))
                .filter(pair -> pair.distance < 6)
                .min(Comparator.comparingInt(a -> a.distance))
                .map(pair -> pair.setTimer)
                .orElse(null);
    }
}
