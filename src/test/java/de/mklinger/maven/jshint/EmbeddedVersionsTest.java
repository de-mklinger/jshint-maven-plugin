package de.mklinger.maven.jshint;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.mklinger.maven.jshint.jshint.EmbeddedJshintCode;
import de.mklinger.maven.jshint.jshint.JSHint;
import de.mklinger.maven.jshint.jshint.JSHint.Hint;

@RunWith(Parameterized.class)
public class EmbeddedVersionsTest {
    private static final String UTF_8 = "UTF-8";

    public static class OutputMessagesVariant {
        protected String expectedEvalIsEvilMessage(){
            return "eval can be harmful.";
        }

        protected String expectedErrorMessageForTwoTooManyParameters(){
            return "This function has too many parameters. (2)";
        }
    }

    @Parameters(name = "Test for compatiblity with jshint version {0}")
    public static Collection<Object[]> data() {
        final List<Object[]> params = new ArrayList<Object[]>();
        for (final String version : EmbeddedJshintCode.EMBEDDED_VERSIONS.keySet()){
            params.add(new Object[]{ version });
        }
        return params;
    }

    private final String jshintVersion;
    private final OutputMessagesVariant variants;

    public EmbeddedVersionsTest(final String jshintVersion) {
        super();
        this.jshintVersion = EmbeddedJshintCode.EMBEDDED_VERSIONS.get(jshintVersion);

        if (jshintVersion.equals("r12")) {
            variants = new OutputMessagesVariant(){
                @Override
                protected String expectedErrorMessageForTwoTooManyParameters() {
                    return "Too many parameters per function (2).";
                }

                @Override
                protected String expectedEvalIsEvilMessage() {
                    return "eval is evil.";
                }
            };
        } else {
            variants = new OutputMessagesVariant();
        }
    }


    @Test
    public void booleanOptionsCanBeFalse() throws IOException{
        // given
        final String globals = "";
        final String options = "evil:false";
        final InputStream code = toStream("eval('var x = 1 + 1;');");
        final JSHint jsHint = new JSHint(jshintVersion, UTF_8);

        // when
        final List<Hint> hints = jsHint.run(code, options, globals);

        // then
        Assert.assertNotNull(hints);
        Assert.assertEquals(1, hints.size());
        Assert.assertEquals(variants.expectedEvalIsEvilMessage(), hints.get(0).reason);
    }



    @Test
    public void booleanOptionsCanBeTrue() throws IOException{
        // given
        final String globals = "";
        final String options = "evil:true";
        final InputStream code = toStream("eval('var x = 1 + 1;');");
        final JSHint jsHint = new JSHint(jshintVersion, UTF_8);

        // when
        final List<Hint> hints = jsHint.run(code, options, globals);

        // then
        Assert.assertNotNull(hints);
        Assert.assertEquals(0, hints.size());
    }

    @Test
    public void supportsOptionsThatTakeANumericValue() throws IOException {
        if (jshintVersion.startsWith("jshint-rhino-2.5.")) {
            // Since 2.5.0 - See https://github.com/jshint/jshint/releases/tag/2.5.0
            // indent no longer provides warnings about indentation levels
            return;
        }

        // given
        final String globals = "alert";
        final String options = "indent:4";
        final InputStream code = toStream(" alert('Bad Indentation');");
        final JSHint jsHint = new JSHint(jshintVersion, UTF_8);

        // when
        final List<Hint> hints = jsHint.run(code, options, globals);

        // then
        Assert.assertNotNull(hints);
        Assert.assertEquals(1, hints.size());
        Assert.assertEquals("Expected 'alert' to have an indentation at 1 instead at 2.", hints.get(0).reason);
    }

    @Test
    public void supportsParametersWithValues() throws IOException{
        // given
        final String globals = "";
        final String options = "maxparams:1";
        final InputStream code = toStream("function cowboyFunction(param1, param2){return 'yee-haw!';}");
        final JSHint jsHint = new JSHint(jshintVersion, UTF_8);

        // when
        final List<Hint> hints = jsHint.run(code, options, globals);

        // then
        Assert.assertNotNull(hints);
        Assert.assertEquals(1, hints.size());
        Assert.assertEquals(variants.expectedErrorMessageForTwoTooManyParameters(), hints.get(0).reason);
    }

    @Test
    public void supportsParametersWithoutValues() throws IOException{
        // given
        final String globals = "Foo";
        final String options = "nonew";
        final InputStream code = toStream("new Foo();");
        final JSHint jsHint = new JSHint(jshintVersion, UTF_8);

        // when
        final List<Hint> hints = jsHint.run(code, options, globals);

        // then
        Assert.assertNotNull(hints);
        Assert.assertEquals(1, hints.size());
        Assert.assertEquals("W031", hints.get(0).code);
        Assert.assertEquals("Do not use 'new' for side effects.", hints.get(0).raw);
    }

    @Test
    public void supportsTheGlobalsParameter() throws IOException{
        // given
        final String globals = "someGlobal";
        final String options = "undef";
        final InputStream code = toStream("(function(){var value = someGlobal();}());");
        final JSHint jsHint = new JSHint(jshintVersion, UTF_8);

        // when
        final List<Hint> hints = jsHint.run(code, options, globals);

        // then
        Assert.assertNotNull(hints);
        Assert.assertEquals("Expected no hints, but received:\n " + toString(hints), 0, hints.size());
    }

    private static InputStream toStream(final String text) throws UnsupportedEncodingException{
        return new ByteArrayInputStream(text.getBytes(UTF_8));
    }

    private static String toString(final List<Hint> hints) {
        final StringBuffer text = new StringBuffer();
        for(final Hint hint: hints){
            text.append(hint.reason + "\n");
        }
        return text.toString();
    }
}
