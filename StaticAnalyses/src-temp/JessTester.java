import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;

import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAXMLizerCLI;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.VirtualInvokeExpr;

public class JessTester {

	private class JimpleStmtSwitch
			extends AbstractStmtSwitch {

		/**
		 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
		 */
		public void caseAssignStmt(final AssignStmt stmt) {
			if (stmt.getLeftOp().getType() instanceof RefType) {
				try {
					final Fact fact = new Fact(rete.findDeftemplate("pointsto"));
					stmt.getLeftOp().apply(vSwitch);
					fact.setSlotValue("lhs", value);
					stmt.getRightOp().apply(vSwitch);
					fact.setSlotValue("rhs", value);
					if (stmt.containsInvokeExpr()) {
						fact.setSlotValue("invocationSite", new Value(stmt.getInvokeExpr()));
					}
					rete.assertFact(fact);
				} catch (JessException _e) {
					_e.printStackTrace();
					throw new RuntimeException(_e);
				}
			} else if (stmt.containsInvokeExpr()) {
				stmt.getRightOp().apply(vSwitch);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
		 */
		public void caseIdentityStmt(final IdentityStmt stmt) {
			if (stmt.getLeftOp().getType() instanceof RefType && !(stmt.getRightOp() instanceof CaughtExceptionRef)) {
				try {
					final Fact fact = new Fact(rete.findDeftemplate("pointsto"));
					stmt.getLeftOp().apply(vSwitch);
					fact.setSlotValue("lhs", value);
					stmt.getRightOp().apply(vSwitch);
					fact.setSlotValue("rhs", value);
					rete.assertFact(fact);
				} catch (JessException _e) {
					_e.printStackTrace();
					throw new RuntimeException(_e);
				}
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
		 */
		public void caseInvokeStmt(final InvokeStmt stmt) {
			stmt.getInvokeExpr().apply(vSwitch);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
		 */
		public void caseReturnStmt(final ReturnStmt stmt) {
			try {
				final Fact fact = new Fact(rete.findDeftemplate("pointsto"));
				fact.setSlotValue("lhs", new Value(sm.getSignature() + "+returnCallee"));
				stmt.getOp().apply(vSwitch);
				fact.setSlotValue("rhs", value);
				fact.setSlotValue("index", new Value(-2, RU.INTEGER));
				rete.assertFact(fact);
			} catch (JessException _e) {
				_e.printStackTrace();
				throw new RuntimeException(_e);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
		 */
		public void caseThrowStmt(final ThrowStmt stmt) {
			try {
				final Fact fact = new Fact(rete.findDeftemplate("pointsto"));
				fact.setSlotValue("lhs", new Value(sm.getSignature() + "+throwCallee"));
				stmt.getOp().apply(vSwitch);
				fact.setSlotValue("rhs", value);
				fact.setSlotValue("index", new Value(-3, RU.INTEGER));
				rete.assertFact(fact);
			} catch (JessException _e) {
				_e.printStackTrace();
				throw new RuntimeException(_e);
			}
		}
	}

	private class JimpleValueSwitch
			extends AbstractJimpleValueSwitch {

		/**
		 * @see soot.jimple.RefSwitch#caseArrayRef(soot.jimple.ArrayRef)
		 */
		public void caseArrayRef(final ArrayRef v) {
			value = new Value(v.getType());
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseCastExpr(soot.jimple.CastExpr)
		 */
		public void caseCastExpr(final CastExpr v) {
			v.getOp().apply(this);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseInstanceFieldRef(soot.jimple.InstanceFieldRef)
		 */
		public void caseInstanceFieldRef(final InstanceFieldRef v) {
			if (v.getField().getType() instanceof RefType) {
				value = new Value(v.getField());
			}
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseInterfaceInvokeExpr(soot.jimple.InterfaceInvokeExpr)
		 */
		public void caseInterfaceInvokeExpr(final InterfaceInvokeExpr v) {
			caseInstanceInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.JimpleValueSwitch#caseLocal(soot.Local)
		 */
		public void caseLocal(final Local l) {
			addTypeForProcessing(l.getType());
			value = new Value(l);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNewArrayExpr(soot.jimple.NewArrayExpr)
		 */
		public void caseNewArrayExpr(final NewArrayExpr v) {
			addTypeForProcessing(((ArrayType)v.getType()).getElementType());
			value = new Value(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNewExpr(soot.jimple.NewExpr)
		 */
		public void caseNewExpr(final NewExpr v) {
			addTypeForProcessing(v.getType());
			value = new Value(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNewMultiArrayExpr(soot.jimple.NewMultiArrayExpr)
		 */
		public void caseNewMultiArrayExpr(final NewMultiArrayExpr v) {
			addTypeForProcessing(((ArrayType)v.getType()).getElementType());
			value = new Value(v);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseNullConstant(soot.jimple.NullConstant)
		 */
		public void caseNullConstant(final NullConstant v) {
			value = new Value(NULL);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseParameterRef(soot.jimple.ParameterRef)
		 */
		public void caseParameterRef(final ParameterRef v) {
			if (v.getType() instanceof RefType) {
				value = new Value(sm.getSignature() + "+" + v.getIndex());
			}
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseSpecialInvokeExpr(soot.jimple.SpecialInvokeExpr)
		 */
		public void caseSpecialInvokeExpr(final SpecialInvokeExpr v) {
			caseInstanceInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseStaticFieldRef(soot.jimple.StaticFieldRef)
		 */
		public void caseStaticFieldRef(final StaticFieldRef v) {
			value = new Value(v.getField());
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseStaticInvokeExpr(soot.jimple.StaticInvokeExpr)
		 */
		public void caseStaticInvokeExpr(final StaticInvokeExpr v) {
			try {
				for (int i = 0; i < v.getMethod().getParameterCount(); i++) {
					if (v.getArg(i).getType() instanceof RefType) {
						addTypeForProcessing(v.getArg(i).getType());

						final Fact fact = new Fact(rete.findDeftemplate("pointsto"));
						fact.setSlotValue("lhs", new Value(v.getMethod().getSignature() + "+" + i));
						v.getArg(i).apply(this);
						fact.setSlotValue("rhs", value);
						rete.assertFact(fact);
					}
				}

				addClassForProcessing(v.getMethod().getDeclaringClass());

				if (v.getMethod().getReturnType() instanceof RefType) {
					addTypeForProcessing(v.getMethod().getReturnType());
					value = new Value(v.getMethod().getSignature() + "+returnCaller");
				}

				addMethodForProcessing(v.getMethod());
			} catch (JessException _e) {
				_e.printStackTrace();
				throw new RuntimeException(_e);
			}

		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseStringConstant(soot.jimple.StringConstant)
		 */
		public void caseStringConstant(final StringConstant v) {
			addTypeForProcessing(v.getType());
			final String _string = v.value;
			_string.intern();
			value = new Value(_string);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseThisRef(soot.jimple.ThisRef)
		 */
		public void caseThisRef(final ThisRef v) {
			value = new Value(sm.getSignature() + "+thisRefCallee");
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseVirtualInvokeExpr(soot.jimple.VirtualInvokeExpr)
		 */
		public void caseVirtualInvokeExpr(final VirtualInvokeExpr v) {
			caseInstanceInvokeExpr(v);
		}

		/**
		 * DOCUMENT ME!
		 *
		 * @param v
		 */
		private void caseInstanceInvokeExpr(final InstanceInvokeExpr v) {
			try {
				for (int i = 0; i < v.getMethod().getParameterCount(); i++) {
					if (v.getArg(i).getType() instanceof RefType) {
						addTypeForProcessing(v.getArg(i).getType());
						final Fact fact = new Fact(rete.findDeftemplate("pointsto"));
						fact.setSlotValue("lhs", new Value(v.getMethod().getSignature() + "+" + i));
						v.getArg(i).apply(this);
						fact.setSlotValue("rhs", value);
						fact.setSlotValue("invocationSite", new Value(v));
						fact.setSlotValue("index", new Value(i, RU.INTEGER));
						rete.assertFact(fact);
					}
				}

				final Fact fact1 = new Fact(rete.findDeftemplate("pointsto"));
				fact1.setSlotValue("lhs", new Value(v.getMethod().getSignature() + "+thisRefCaller"));
				v.getBase().apply(this);
				fact1.setSlotValue("rhs", value);
				fact1.setSlotValue("invocationSite", new Value(v));
				fact1.setSlotValue("index", new Value(-1, RU.INTEGER));
				rete.assertFact(fact1);

				addClassForProcessing(v.getMethod().getDeclaringClass());

				if (v.getMethod().getReturnType() instanceof RefType) {
					addTypeForProcessing(v.getMethod().getReturnType());
					value = new Value(v.getMethod().getSignature() + "+returnCaller");
				}
			} catch (JessException _e) {
				_e.printStackTrace();
				throw new RuntimeException(_e);
			}
		}
	}

	/**
	 * Logger for this class
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(JessTester.class);

	private static final NullConstant NULL = NullConstant.v();

	Rete rete = new Rete();

	SootClass sc;

	Scene scene;

	SootMethod sm;

	Value value;

	private int methodCount = 0;

	final IWorkBag<SootMethod> methodsWorkBag = new HistoryAwareFIFOWorkBag<SootMethod>(new HashSet<SootMethod>());

	final IWorkBag<SootClass> typesWorkBag = new HistoryAwareFIFOWorkBag<SootClass>(new HashSet<SootClass>());

	private final JimpleStmtSwitch sSwitch = new JimpleStmtSwitch();

	private final JimpleValueSwitch vSwitch = new JimpleValueSwitch();

	private int classCount = 0;

	/**
	 * DOCUMENT ME!
	 *
	 * @param args
	 */
	public static void main(final String[] args) {
		final Options _options = new Options();
		Option _option = new Option("o", "output", true, "Directory into which xml files will be written into.");
		_option.setArgs(1);
		_options.addOption(_option);
		_option = new Option("p", "soot-classpath", false, "Prepend this to soot class path.");
		_option.setArgs(1);
		_option.setArgName("classpath");
		_option.setOptionalArg(false);
		_options.addOption(_option);

		final PosixParser _parser = new PosixParser();

		try {
			final CommandLine _cl = _parser.parse(_options, args);

			if (_cl.hasOption("h")) {
				final String _cmdLineSyn = "java " + OFAXMLizerCLI.class.getName() + " <options> <classnames>";
				(new HelpFormatter()).printHelp(_cmdLineSyn, "Options are: ", _options, "");
				System.exit(1);
			}

			String _outputDir = _cl.getOptionValue('o');

			if (_outputDir == null) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Defaulting to current directory for output.");
				}
				_outputDir = ".";
			}

			final Scene _scene = Scene.v();
			soot.options.Options.v().parse(Util.getSootOptions());

			if (_cl.hasOption('p')) {
				_scene.setSootClassPath(_cl.getOptionValue('p'));
			}

			for (final String _cls : _cl.getArgs()) {
				_scene.loadClassAndSupport(_cls);
			}

			final JessTester _jt = new JessTester();
			_jt.scene = _scene;
			_jt.processScene(_cl.getArgList());
		} catch (final ParseException _e) {
			LOGGER.error("Error while parsing command line.", _e);
			final String _cmdLineSyn = "java " + OFAXMLizerCLI.class.getName() + " <options> <classnames>";
			(new HelpFormatter()).printHelp(_cmdLineSyn, "Options are: ", _options, "");
		} catch (final Throwable _e) {
			LOGGER.error("Beyond our control. May day! May day!", _e);
			throw new RuntimeException(_e);
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param type DOCUMENT ME!
	 */
	void addTypeForProcessing(final Type type) {
		if (type instanceof RefType) {
			typesWorkBag.addWorkNoDuplicates(((RefType)type).getSootClass());
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param clazz DOCUMENT ME!
	 */
	void addClassForProcessing(final SootClass clazz) {
		typesWorkBag.addWorkNoDuplicates(clazz);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param method DOCUMENT ME!
	 */
	void addMethodForProcessing(final SootMethod method) {
		methodsWorkBag.addWorkNoDuplicates(method);
	}


	/**
	 * DOCUMENT ME!
	 *
	 * @param clazz DOCUMENT ME!
	 */
	private void processClassInitializer(final SootClass clazz) {
		if (clazz.declaresMethodByName("<clinit>")) {
			processMethod(clazz.getMethodByName("<clinit>"));
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param method DOCUMENT ME!
	 */
	private void processMethod(final SootMethod method) {
		sm = method;
		if (method.isConcrete()) {
			System.out.println("MProcessing" + ++methodCount + ") " + method.getSignature());
			for (final Object _except : method.getExceptions()) {
				addClassForProcessing(((RefType)_except).getSootClass());
			}
			final Collection<Stmt> _stmts = sm.retrieveActiveBody().getUnits();
			for (final Iterator<Stmt> _k = _stmts.iterator(); _k.hasNext();) {
				final Stmt _stmt = _k.next();
				_stmt.apply(sSwitch);
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * @param clazz DOCUMENT ME!
	 */
	private void processClass(final SootClass clazz) {
		classCount++;
		System.out.println("CProcessing " + classCount  + ") " + clazz);
		processClassInitializer(clazz);
		for (final Object _super : clazz.getInterfaces()) {
			addClassForProcessing((SootClass) _super);
		}
		if (clazz.hasSuperclass()) {
			addClassForProcessing(clazz.getSuperclass());
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param v DOCUMENT ME!
	 * @param e DOCUMENT ME!
	 */
	public void resolveInvocation(final Object v, final InstanceInvokeExpr e) {
		final SootClass _sc;
		if (v instanceof NewExpr) {
			_sc = ((NewExpr) v).getBaseType().getSootClass();
		} else if (v instanceof NewArrayExpr || v instanceof NewMultiArrayExpr) {
			_sc = scene.getSootClass("java.lang.Object");
		} else if (v instanceof StringConstant) {
			 _sc = scene.getSootClass("java.lang.String");
		} else {
			throw new RuntimeException("Inappropriate type at receiver site - " + e + " -- " + v);
		}
		try {
			addMethodForProcessing(Util.findDeclaringMethod(_sc, e.getMethod()));
		} catch (IllegalStateException _e) {
			System.out.println(":-( Failed!! " + e + " -- " + v);
			_e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 * @param classes DOCUMENT ME!
	 */
	private void processScene(final Collection<String> classes) {
		try {
			rete.store("Engine", this);
			rete.executeCommand("(watch rules)");
			//rete.executeCommand("(watch activations)");
			rete.executeCommand("(import soot.jimple.*)");
			rete.executeCommand("(deftemplate pointsto (slot lhs) (slot rhs) (slot invocationSite (default nil)) "
					+ "(slot index (default -4)))");
			rete.executeCommand("(deftemplate equiv (slot one) (slot two))");
			/* rete.executeCommand("(defrule equivRule (and (pointsto (lhs ?a) (rhs ?b) (invocationSite ?) (index ?)) "
					+ "(pointsto (lhs ?b) (rhs ?a) (invocationSite ?) (index ?))) => "
					+ "(assert (equiv (one ?a) (two ?b))))"); */
			rete.executeCommand("(deffunction ifThis (?i1 ?i2) (if (or (= ?i1 -1) (= ?i2 -1)) then -1 else -4))");
			rete.executeCommand("(defrule flowRule (and "
					+ "(and (pointsto (lhs ?a) (rhs ?b) (invocationSite ?) (index ?i1)) "
					+ "(pointsto (lhs ?c) (rhs ?a) (invocationSite ?s) (index ?i2))) "
					+ "(and (not (equiv (one ?c) (two ?a))) (not (equiv (one ?a) (two ?c))))) => "
					+ "(assert (pointsto (lhs ?c) (rhs ?b) (invocationSite ?s) (index (ifThis ?i1 ?i2)))))");
			rete.executeCommand("(defquery findParamFactsFor \"Finds facts for Parameters at given invocation site\""
					+ "(declare (variables ?callSite)) "
					+ "(pointsto (lhs ?a) (rhs ?b) (invocationSite ?callSite) (index ?i&:(> ?i -1))))");
			rete.executeCommand("(defquery findReturnFactsFor \"Finds facts for returned values at given invocation site\""
					+ "(declare (variables ?callSite)) "
					+ "(pointsto (lhs ?a) (rhs ?b) (invocationSite ?callSite) (index ?i&:(= ?i -2))))");
			rete.executeCommand("(defquery findThrowFactsFor \"Finds facts for Thrown exceptions at given invocation site\""
					+ "(declare (variables ?callSite)) "
					+ "(pointsto (lhs ?a) (rhs ?b) (invocationSite ?callSite) (index ?i&:(= ?i -3))))");
			rete.executeCommand("(defrule connectionRule (pointsto (lhs ?a) (rhs ?b) (invocationSite ?d&~nil) (index -1)) "
					+ "=> (bind ?it (run-query findParamFactsFor ?d)) "
					+ "(bind ?callee (call (call ?d getMethod) getSignature)) "
					+ "(while (call ?it hasNext) do "
					+ "(bind ?token (call ?it next)) "
					+ "(bind ?fact (call ?token fact 1)) "
					+ "(bind ?invocationSite (fact-slot-value ?fact invocationSite)) "
					+ "(bind ?method (call ?invocationSite getMethod)) "
					+ "(bind ?callSite (call ?method getSignature)) "
					+ "(bind ?index (fact-slot-value ?fact index)) "
					+ "(bind ?rhs (str-cat ?callSite \"+\" ?index)) "
					+ "(bind ?lhs (str-cat ?callee \"+\" ?index)) "
					+ "(assert (pointsto (lhs ?lhs) (rhs ?rhs))))"
					+ "(bind ?it (run-query findReturnFactsFor ?d)) "
					+ "(bind ?callee (call (call ?d getMethod) getSignature)) "
					+ "(while (call ?it hasNext) do "
					+ "(bind ?token (call ?it next)) "
					+ "(bind ?fact (call ?token fact 1)) "
					+ "(bind ?invocationSite (fact-slot-value ?fact invocationSite)) "
					+ "(bind ?method (call ?invocationSite getMethod)) "
					+ "(bind ?callSite (call ?method getSignature)) "
					+ "(bind ?rhs (str-cat ?callee \"+returnCallee\")) "
					+ "(bind ?lhs (str-cat ?callSite \"+returnCaller\")) "
					+ "(assert (pointsto (lhs ?lhs) (rhs ?rhs))))"
					+ ")");
			rete.executeCommand("(defrule expansionRule ?fact <- (pointsto (lhs ?a) (rhs ?b) (invocationSite ?c) (index -1))"
					+ "=> (if (or (or (or (instanceof ?b soot.jimple.NewExpr) (instanceof ?b soot.jimple.NewArrayExpr)) "
					+ "(instanceof ?b soot.jimple.NewMultiArrayExpr)) (instanceof ?b soot.jimple.StringConstant)) then "
					+ "(printout t (?a toString) (?b toString)) (call (fetch Engine) resolveInvocation ?b ?c)))");

			for (final String _className : classes) {
				sc = scene.getSootClass(_className);
				addClassForProcessing(sc);
				for (final Iterator<SootMethod> _j = sc.getMethods().iterator(); _j.hasNext();) {
					addMethodForProcessing(_j.next());
				}
			}

			while (typesWorkBag.hasWork() || methodsWorkBag.hasWork()) {
				while (typesWorkBag.hasWork()) {
					processClass(typesWorkBag.getWork());
				}
				while (methodsWorkBag.hasWork()) {
					processMethod(methodsWorkBag.getWork());
				}
				rete.run();
			}

			rete.run();

			final StringWriter _sw = new StringWriter();
			rete.ppFacts(_sw);
			System.out.println(_sw.toString());
		} catch (JessException _e) {
			_e.printStackTrace();
			throw new RuntimeException(_e);
		} catch (IOException _e) {
			_e.printStackTrace();
			throw new RuntimeException(_e);
		}
	}
}

// End of File
