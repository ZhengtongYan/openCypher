package org.opencypher.tools.grammar;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.opencypher.grammar.Grammar;
import org.opencypher.tools.output.Output;

import static org.junit.Assert.assertThat;
import static org.opencypher.grammar.Grammar.charactersOfSet;
import static org.opencypher.grammar.Grammar.grammar;
import static org.opencypher.grammar.Grammar.literal;
import static org.opencypher.grammar.Grammar.nonTerminal;
import static org.opencypher.grammar.Grammar.optional;
import static org.opencypher.tools.grammar.Antlr4ToolFacade.assertGeneratesValidParser;
import static org.opencypher.tools.output.Output.stringBuilder;

public class Antlr4Test
{
    @Test
    public void shouldProduceSimpleParserGrammar() throws Exception
    {
        assertGenerates(
                grammar( "MyLanguage" )
                        .production( "MyLanguage", nonTerminal( "value" ) )
                        .production( "value", nonTerminal( "alpha" ), nonTerminal( "beta" ) )
                        .production( "alpha", literal( "a" ) )
                        .production( "beta", literal( "b" ) ),
                "grammar MyLanguage;",
                "",
                "myLanguage : value ;",
                "",
                "value : alpha",
                "      | beta",
                "      ;",
                "",
                "alpha : 'a' ;",
                "",
                "beta : 'b' ;",
                "" );
    }

    @Test
    public void shouldEncloseOptionalSequence() throws Exception
    {
        assertGenerates(
                grammar( "FooBar" )
                        .production( "FooBar", optional( literal( "foo" ), literal( "bar" ) ) ),
                "grammar FooBar;",
                "",
                "fooBar : ( 'foo' 'bar' )? ;",
                "" );
    }

    @Test
    public void shouldGenerateCharacterSet() throws Exception
    {
        assertCharset( "White_Space",
                       "[\\t\\n\\u000B\\f\\r \\u0085\\u00A0\\u1680" +
                       "\\u2000\\u2001\\u2002\\u2003\\u2004\\u2005" +
                       "\\u2006\\u2007\\u2008\\u2009\\u200A" +
                       "\\u2028\\u2029\\u202F\\u205F\\u3000]" );
    }

    @Test
    public void shouldGenerateCharacterSetWithExceptions() throws Exception
    {
        assertGenerates(
                grammar( "test" )
                        .production( "test", charactersOfSet( "Sc" ).except(
                                '\u058F', '\u060B', '\u09F2', '\u09F3', '\u09FB', '\u0AF1',
                                '\u0BF9', '\u0E3F', '\u17DB', '\uA838', '\uFDFC', '\uFE69',
                                '\uFF04', '\uFFE0', '\uFFE1', '\uFFE5', '\uFFE6' ) ),
                "grammar test;",
                "",
                "test : TEST_0 ;",
                "",
                "TEST_0 : [$\\u00A2-\\u00A5\\u20A0-\\u20BA] ;",
                "" );
    }

    static void assertCharset( String name, String def )
    {
        assertGenerates(
                grammar( "test" )
                        .production( "test", charactersOfSet( name ) ),
                "grammar test;",
                "",
                "test : " + name + " ;",
                "",
                name + " : " + def + " ;",
                "" );
    }

    @Test
    public void shouldGenerateCypherGrammar() throws Exception
    {
        assertGeneratesValidParser( "/cypher.xml" );
    }

    static void assertGenerates( Grammar.Builder grammar, String... lines )
    {
        assertGenerates( grammar.build(), lines );
    }

    static void assertGenerates( Grammar grammar, String... lines )
    {
        Output.Readable result = stringBuilder();
        Antlr4.write( grammar, result );
        assertThat( result, contentsEquals( lines ) );
    }

    static Matcher<Output.Readable> contentsEquals( String... lines )
    {
        String expected = Output.lines( lines );
        return new TypeSafeMatcher<Output.Readable>()
        {
            @Override
            protected boolean matchesSafely( Output.Readable item )
            {
                return item.contentsEquals( expected );
            }

            @Override
            public void describeTo( Description description )
            {
                description.appendValue( expected );
            }
        };
    }
}
