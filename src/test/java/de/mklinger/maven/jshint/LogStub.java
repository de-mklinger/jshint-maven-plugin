package de.mklinger.maven.jshint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;

public class LogStub implements Log {
    public static class Message {
        final CharSequence content;
        final Throwable error;

        public Message(final CharSequence content, final Throwable error) {
            super();
            this.content = content;
            this.error = error;
        }

    }
    private final Map<String, List<Message>> messages = new HashMap<String, List<Message>>();

    private void log(final String level, final CharSequence content){
        log(level, content, null);
    }

    private void log(final String level, final Throwable error){
        log(level, null, error);
    }

    private void log(final String level, final CharSequence content, final Throwable error){
        System.out.println("[" + getClass().getSimpleName() + "@" + level + "] " + content);
        final List<Message> levelMessages = messagesForLevel(level);
        levelMessages.add(new Message(content, error));
    }

    public boolean hasMessage(final String level, final String messageContent){
        final List<Message> levelMessages = messagesForLevel(level);
        for(final Message m : levelMessages){
            final String content = m.content.toString();
            if(content.equals(messageContent)) {
                return true;
            }
        }

        return false;
    }

    public List<Message> messagesForLevel(final String level) {
        List<Message> levelMessages = messages.get(level);
        if(levelMessages==null){
            levelMessages = new ArrayList<LogStub.Message>();
            messages.put(level, levelMessages);
        }
        return levelMessages;
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(final CharSequence content) {
        log("debug", content);
    }

    @Override
    public void debug(final CharSequence content, final Throwable error) {
        log("debug", content, error);
    }

    @Override
    public void debug(final Throwable error) {
        log("debug", error);
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void info(final CharSequence content) {
        log("info", content);
    }

    @Override
    public void info(final CharSequence content, final Throwable error) {
        log("info", content, error);
    }

    @Override
    public void info(final Throwable error) {
        log("info", error);
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(final CharSequence content) {
        log("warn", content);
    }

    @Override
    public void warn(final CharSequence content, final Throwable error) {
        log("warn", content, error);
    }

    @Override
    public void warn(final Throwable error) {
        log("warn", error);
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(final CharSequence content) {
        log("error", content);
    }

    @Override
    public void error(final CharSequence content, final Throwable error) {
        log("error", content, error);
    }

    @Override
    public void error(final Throwable error) {
        log("error", error);
    }
}
