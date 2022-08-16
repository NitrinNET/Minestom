package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.CommandCondition;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static org.junit.jupiter.api.Assertions.*;

public class GraphConversionExecutorTest {
    @Test
    public void defaultCondition() {
        final Command foo = new Command("foo");
        // Constant true
        {
            foo.setCondition((sender, commandString) -> true);
            var graph = Graph.fromCommand(foo);
            var execution = graph.root().execution();
            assertNotNull(execution);
            assertTrue(execution.test(null));
        }
        // Constant false
        {
            foo.setCondition((sender, commandString) -> false);
            var graph = Graph.fromCommand(foo);
            var execution = graph.root().execution();
            assertNotNull(execution);
            assertFalse(execution.test(null));
        }
    }

    @Test
    public void emptySyntaxCondition() {
        final Command foo = new Command("foo");
        foo.addSyntax(GraphConversionExecutorTest::dummyExecutor, Literal("first"));

        var graph = Graph.fromCommand(foo);
        assertEquals(1, graph.root().next().size());
        var execution = graph.root().next().get(0).execution();
        assertNotNull(execution);
        assertNull(execution.condition());
        assertNotNull(execution.executor());
    }

    @Test
    public void syntaxConditionTrue() {
        final Command foo = new Command("foo");
        foo.addConditionalSyntax((sender, context) -> true,
                GraphConversionExecutorTest::dummyExecutor, Literal("first"));

        var graph = Graph.fromCommand(foo);
        assertEquals(1, graph.root().next().size());
        var execution = graph.root().next().get(0).execution();
        assertNotNull(execution);
        assertTrue(execution.test(null));
    }

    @Test
    public void syntaxConditionFalse() {
        final Command foo = new Command("foo");
        foo.addConditionalSyntax((sender, context) -> false,
                GraphConversionExecutorTest::dummyExecutor, Literal("first"));

        var graph = Graph.fromCommand(foo);
        assertEquals(1, graph.root().next().size());
        var execution = graph.root().next().get(0).execution();
        assertNotNull(execution);
        assertFalse(execution.test(null));
    }

    @Test
    public void commandConditionFalse() {
        final Command foo = new Command("foo");
        foo.setCondition((sender, commandString) -> false);
        final Graph graph = Graph.fromCommand(foo);
        final Graph.Execution execution = graph.root().execution();
        assertNotNull(execution);
        final CommandCondition condition = execution.condition();
        assertNotNull(condition);
        assertFalse(condition.canUse(null, null));
    }


    @Test
    public void issue1326() {
        final AtomicBoolean b = new AtomicBoolean(true);
        var cmd = new Command("tp") {{
            addConditionalSyntax((s, c) -> b.getAndSet(false), null, ArgumentType.Entity("target"),
                    ArgumentType.Double("x"), ArgumentType.Double("y"), ArgumentType.Double("z"));
        }};
        var graph = Graph.fromCommand(cmd);
        final Graph.Node target = graph.root().next().get(0);
        final Graph.Execution execution = target.execution();
        assertNotNull(execution);
        final CommandCondition condition = execution.condition();
        assertNotNull(condition);
        assertTrue(condition.canUse(null, null));
        assertFalse(b.get(), "Condition wasn't executed.");
    }

    private static void dummyExecutor(CommandSender sender, CommandContext context) {
    }
}
