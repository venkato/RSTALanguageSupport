/*
 * 12/14/2010
 *
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This code is licensed under the LGPL.  See the "license.txt" file included
 * with this project.
 */
package org.fife.rsta.ac.java;

import java.util.List;
import javax.swing.text.JTextComponent;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.java.rjc.ast.CompilationUnit;
import org.fife.rsta.ac.java.rjc.ast.TypeDeclaration;
import org.fife.ui.autocomplete.ParameterChoicesProvider;
import org.fife.ui.autocomplete.ParameterizedCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * A parameter choices provider for Java methods.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class SourceParamChoicesProvider implements ParameterChoicesProvider {


	public SourceParamChoicesProvider() {
		
	}


	/**
	 * {@inheritDoc}
	 */
	public List getParameterChoices(JTextComponent tc,
								ParameterizedCompletion.Parameter param) {

		// Get the language support for Java
		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport support = lsf.getSupportFor(SyntaxConstants.
													SYNTAX_STYLE_JAVA);
		JavaLanguageSupport jls = (JavaLanguageSupport)support;

		// Get the deepest TypeDeclaration AST containing the caret position
		// for the source code in the editor.
		JavaParser parser = jls.getParser((RSyntaxTextArea)tc);
		if (parser==null) {
			return null;
		}
		CompilationUnit cu = parser.getCompilationUnit();
		if (cu==null) {
			return null;
		}
		int dot = tc.getCaretPosition();
		TypeDeclaration typeDec = cu.getDeepestTypeDeclarationAtOffset(dot);
		if (typeDec==null) {
			return null;
		}

		List list = typeDec.getAccessibleMembersOfType(param.getType(), dot);
		return list;

	}


}