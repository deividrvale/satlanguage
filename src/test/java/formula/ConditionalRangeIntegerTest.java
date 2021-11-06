import org.junit.Test;
import static org.junit.Assert.*;

import logic.sat.Variable;
import logic.sat.Atom;
import logic.sat.Clause;
import logic.number.range.RangeInteger;
import logic.number.range.RangeConstant;
import logic.number.range.RangeVariable;
import logic.formula.Formula;
import logic.formula.And;
import logic.formula.AtomicFormula;
import logic.formula.ConditionalRangeInteger;
import java.util.ArrayList;

public class ConditionalRangeIntegerTest {
  private Atom truth() {
    return new Atom(new Variable("TRUE"), true);
  }

  private Formula makeAtom(String varname, boolean value) {
    Variable x = new Variable(varname);
    return new AtomicFormula(new Atom(x, value));
  }

  @Test
  public void testUnboundedToString() {
    RangeInteger a = new RangeVariable("a", 1, 5, truth());
    Formula z = makeAtom("z", true);
    RangeInteger cri = new ConditionalRangeInteger(z, a, truth());
    assertTrue(cri.toString().equals("z?a"));
  }

  @Test
  public void testSemiBoundedToString() {
    RangeInteger a = new RangeVariable("a", 1, 5, truth());
    Formula z = new And(makeAtom("z", true), makeAtom("q", false));
    RangeInteger cri = new ConditionalRangeInteger(z, a, truth(), 0, 6);
    assertTrue(cri.toString().equals("⟦z ∧ ¬q⟧?a"));
  }

  @Test
  public void testBoundedToString() {
    RangeInteger a = new RangeVariable("a", 1, 5, truth());
    Formula z = makeAtom("z", true);
    RangeInteger cri = new ConditionalRangeInteger(z, a, truth(), 0, 4);
    assertTrue(cri.toString().equals("cond(0, 4, z?a)"));
  }

  @Test
  public void testPositiveConstant() {
    RangeInteger n = new RangeConstant(4, truth());
    Formula x = makeAtom("x", true);
    RangeInteger cri = new ConditionalRangeInteger(x, n, truth());
    assertTrue(cri.queryGeqAtom(-1).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(0).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(1).toString().equals("x"));
    assertTrue(cri.queryGeqAtom(4).toString().equals("x"));
    assertTrue(cri.queryGeqAtom(5).toString().equals("¬TRUE"));

    ClauseCollector col = new ClauseCollector();
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 0);
  }

  @Test
  public void testNegativeConstant() {
    RangeInteger n = new RangeConstant(-4, truth());
    Formula x = new And(makeAtom("x", false), makeAtom("y", true));
    RangeInteger cri = new ConditionalRangeInteger(x, n, truth());
    assertTrue(cri.queryGeqAtom(1).toString().equals("¬TRUE"));
    assertTrue(cri.queryGeqAtom(0).toString().equals("¬⟦¬x ∧ y⟧"));
    assertTrue(cri.queryGeqAtom(-3).toString().equals("¬⟦¬x ∧ y⟧"));
    assertTrue(cri.queryGeqAtom(-4).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(-5).toString().equals("TRUE"));

    ClauseCollector col = new ClauseCollector();
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 3);    // only the clauses defining ⟦x ∧ y⟧
    assertTrue(col.contains("x ∨ ¬y ∨ ⟦¬x ∧ y⟧"));
    assertTrue(col.contains("¬x ∨ ¬⟦¬x ∧ y⟧"));
    assertTrue(col.contains("y ∨ ¬⟦¬x ∧ y⟧"));
  }

  @Test
  public void testPositiveRange() {
    Variable.reset();
    Formula x = makeAtom("x", true);
    RangeInteger y = new RangeVariable("y", 2, 5, truth());
    RangeInteger cri = new ConditionalRangeInteger(x, y, truth());
    assertTrue(cri.queryGeqAtom(-1).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(0).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(1).toString().equals("x"));
    assertTrue(cri.queryGeqAtom(2).toString().equals("x"));
    assertTrue(cri.queryGeqAtom(3).toString().equals("x?y≥3"));
    assertTrue(cri.queryGeqAtom(4).toString().equals("x?y≥4"));
    assertTrue(cri.queryGeqAtom(5).toString().equals("x?y≥5"));
    assertTrue(cri.queryGeqAtom(6).toString().equals("¬TRUE"));

    ClauseCollector col = new ClauseCollector();
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 9);  // 3 clauses for each of the variables we defined
    assertTrue(col.contains("x ∨ ¬x?y≥4"));         // x?y≥4 <-> x /\ y≥4
    assertTrue(col.contains("y≥4 ∨ ¬x?y≥4"));
    assertTrue(col.contains("¬x ∨ ¬y≥4 ∨ x?y≥4"));
  }

  @Test
  public void testNegativeRange() {
    Variable.reset();
    Formula x = new And(makeAtom("x", true), makeAtom("q", false));
    RangeInteger y = new RangeVariable("y", -3, -1, truth());
    RangeInteger cri = new ConditionalRangeInteger(x, y, truth());
    assertTrue(cri.queryGeqAtom(1).toString().equals("¬TRUE"));
    assertTrue(cri.queryGeqAtom(0).toString().equals("¬⟦x ∧ ¬q⟧"));
    assertTrue(cri.queryGeqAtom(-1).toString().equals("⟦x ∧ ¬q⟧?y≥-1"));
    assertTrue(cri.queryGeqAtom(-2).toString().equals("⟦x ∧ ¬q⟧?y≥-2"));
    assertTrue(cri.queryGeqAtom(-3).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(-4).toString().equals("TRUE"));

    ClauseCollector col = new ClauseCollector();
    col.addToMemory("⟦x ∧ ¬q⟧");
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 6);  // 3 clauses for each of the variables we defined
    assertTrue(col.contains("y≥-2 ∨ ¬⟦x ∧ ¬q⟧ ∨ ¬⟦x ∧ ¬q⟧?y≥-2"));     // x?y≥-2 <-> ¬x \/ y≥-2
    assertTrue(col.contains("⟦x ∧ ¬q⟧ ∨ ⟦x ∧ ¬q⟧?y≥-2"));
    assertTrue(col.contains("¬y≥-2 ∨ ⟦x ∧ ¬q⟧?y≥-2"));
  }

  @Test
  public void testNegativePositiveRange() {
    Variable.reset();
    Formula x = makeAtom("x", true);
    RangeInteger y = new RangeVariable("y", -3, 4, truth());
    RangeInteger cri = new ConditionalRangeInteger(x, y, truth());
    assertTrue(cri.queryGeqAtom(-3).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(-2).toString().equals("x?y≥-2"));
    assertTrue(cri.queryGeqAtom(0).toString().equals("x?y≥0"));
    assertTrue(cri.queryGeqAtom(4).toString().equals("x?y≥4"));
    assertTrue(cri.queryGeqAtom(5).toString().equals("¬TRUE"));

    ClauseCollector col = new ClauseCollector();
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 21);  // 3 clauses for each of the variables we defined
    assertTrue(col.contains("¬x ∨ y≥0 ∨ ¬x?y≥0"));  // x?y≥0 <-> ¬x \/ y≥0
    assertTrue(col.contains("x ∨ x?y≥0"));
    assertTrue(col.contains("¬y≥0 ∨ x?y≥0"));
    assertTrue(col.contains("x ∨ ¬x?y≥1"));         // x?y≥1 <-> x /\ y≥1
    assertTrue(col.contains("y≥1 ∨ ¬x?y≥1"));
    assertTrue(col.contains("¬x ∨ ¬y≥1 ∨ x?y≥1"));
  }

  @Test
  public void testRangeWithBounds() {
    RangeInteger a = new RangeVariable("a", -10, 10, truth());
    Formula z = makeAtom("z", true);
    RangeInteger cri = new ConditionalRangeInteger(z, a, truth(), 2, 5);
    assertTrue(cri.queryGeqAtom(-1).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(1).toString().equals("TRUE"));
    assertTrue(cri.queryGeqAtom(3).toString().equals("z?a≥3"));
    assertTrue(cri.queryGeqAtom(5).toString().equals("z?a≥5"));
    assertTrue(cri.queryGeqAtom(6).toString().equals("¬TRUE"));

    ClauseCollector col = new ClauseCollector();
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 9);
  }

  @Test
  public void testAvoidDuplicateWelldefinednessClauses() {
    RangeInteger a = new RangeVariable("a", -10, 10, truth());
    Formula form = new And(makeAtom("z", true), makeAtom("x", true));
    RangeInteger cri = new ConditionalRangeInteger(form, a, truth(), 2, 5);
    ClauseCollector col = new ClauseCollector();
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 12);   // this is the usual number
    cri.addWelldefinednessClauses(col);
    assertTrue(col.size() == 12);   // nothing should have been added
  }
}

