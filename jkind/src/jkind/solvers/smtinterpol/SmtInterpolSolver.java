package jkind.solvers.smtinterpol;

import java.util.List;
import java.util.Map.Entry;

import jkind.JKindException;
import jkind.lustre.Type;
import jkind.lustre.VarDecl;
import jkind.lustre.values.Value;
import jkind.sexp.Cons;
import jkind.sexp.Sexp;
import jkind.solvers.Model;
import jkind.solvers.Result;
import jkind.solvers.SatResult;
import jkind.solvers.SimpleModel;
import jkind.solvers.Solver;
import jkind.solvers.UnknownResult;
import jkind.solvers.UnsatResult;
import jkind.translation.TransitionRelation;
import de.uni_freiburg.informatik.ultimate.logic.Logics;
import de.uni_freiburg.informatik.ultimate.logic.QuotedObject;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;

public class SmtInterpolSolver extends Solver {
	private final Script script;

	public SmtInterpolSolver(String scratchBase) {
		this.script = SmtInterpolUtil.getScript(scratchBase);
	}

	@Override
	public void initialize() {
		script.setLogic(Logics.QF_UFLIRA);
		script.setOption(":verbosity", 2);
	}

	@Override
	public void assertSexp(Sexp sexp) {
		script.assertTerm(convert(sexp));
	}

	@Override
	public void assertSexp(Sexp sexp, String name) {
		script.assertTerm(convert(sexp));
	}

	@Override
	public void define(VarDecl decl) {
		varTypes.put(decl.id, decl.type);
		script.declareFun(decl.id, new Sort[0], getSort(decl.type));
	}

	@Override
	public void define(TransitionRelation lambda) {
		TermVariable[] params = createTermVariables(lambda.getInputs());
		Term definition = convert(params, lambda.getBody());
		script.defineFun(TransitionRelation.T.str, params, script.sort("Bool"), definition);
	}

	private TermVariable[] createTermVariables(List<VarDecl> inputs) {
		return inputs.stream().map(this::createTermVariable).toArray(i -> new TermVariable[i]);
	}

	private TermVariable createTermVariable(VarDecl decl) {
		return script.variable(decl.id, getSort(decl.type));
	}

	@Override
	public Result query(Sexp sexp) {
		Model model;

		push();
		assertSexp(new Cons("not", sexp));

		switch (script.checkSat()) {
		case SAT:
			model = extractModel(script.getModel());
			pop();
			return new SatResult(model);

		case UNSAT:
			pop();
			return new UnsatResult();

		case UNKNOWN:
			model = extractModel(script.getModel());
			pop();
			return new UnknownResult(model);
		}

		throw new JKindException("Unhandled result from solver");
	}

	private Model extractModel(de.uni_freiburg.informatik.ultimate.logic.Model model) {
		SimpleModel result = new SimpleModel();
		for (Entry<String, Type> entry : varTypes.entrySet()) {
			String name = entry.getKey();
			Type type = entry.getValue();
			Term evaluated = model.evaluate(script.term(name));
			Value value = SmtInterpolUtil.getValue(evaluated, type);
			result.addValue(name, value);
		}
		return result;
	}

	@Override
	public void push() {
		script.push(1);
	}

	@Override
	public void pop() {
		script.pop(1);
	}

	@Override
	public void comment(String str) {
		script.echo(new QuotedObject(str));
	}

	@Override
	public void stop() {
	}

	private Sort getSort(Type type) {
		return SmtInterpolUtil.getSort(script, type);
	}

	private Term convert(TermVariable[] params, Sexp sexp) {
		return SmtInterpolUtil.convert(script, params, sexp);
	}

	private Term convert(Sexp sexp) {
		return SmtInterpolUtil.convert(script, sexp);
	}
}
