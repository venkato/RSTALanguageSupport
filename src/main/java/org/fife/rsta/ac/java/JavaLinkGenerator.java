/*
 * 02/17/2013
 *
 * Copyright (C) 2013 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSTALanguageSupport.License.txt file for details.
 */
package org.fife.rsta.ac.java;

import javax.swing.event.HyperlinkEvent;
import javax.swing.text.BadLocationException;

import org.fife.rsta.ac.java.custom.JavaRstaLinkGeneratorResult;
import org.fife.rsta.ac.java.rjc.ast.CompilationUnit;
import org.fife.rsta.ac.java.rjc.ast.Method;
import org.fife.rsta.ac.java.rjc.ast.TypeDeclaration;
import org.fife.ui.rsyntaxtextarea.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * Checks for hyperlink-able tokens under the mouse position when Ctrl is
 * pressed (Cmd on OS X).  Currently this class only checks for accessible
 * members in the current file only (e.g. no members in super classes, no other
 * classes on the classpath, etc.).  So naturally, there is a lot of room for
 * improvement. IDE-style applications, for example, would want to check
 * for members in super-classes, and open their source on click events.
 * 
 * @author Robert Futrell
 * @version 1.0
 */
// TODO: Anonymous inner classes probably aren't handled well.
public class JavaLinkGenerator implements LinkGenerator {
	

    private static final Logger log = Logger.getLogger(JavaLinkGenerator.class.getName());

	private JavaLanguageSupport jls;
	private JavaCompletionProvider javaCompletionProvider;
	private SourceCompletionProvider sourceCompletionProvider;
    private MemberClickedListener memberClickedListener;
    private List<ExternalMemberClickedListener> externalMemberClickedListeners = new ArrayList<ExternalMemberClickedListener>();

	JavaLinkGenerator(JavaLanguageSupport jls, JavaCompletionProvider p) {
		this.jls = jls;
		javaCompletionProvider = p;
		sourceCompletionProvider = (SourceCompletionProvider)javaCompletionProvider.
				getDefaultCompletionProvider();
	}

    JavaLinkGenerator(JavaLanguageSupport jls, JavaCompletionProvider p, MemberClickedListener memberClickedListener) {
        this.jls = jls;
        javaCompletionProvider = p;
        sourceCompletionProvider = (SourceCompletionProvider)javaCompletionProvider.
                getDefaultCompletionProvider();
        this.memberClickedListener = memberClickedListener;
        Thread.dumpStack();
    }

    public void setMemberClickedListener(MemberClickedListener memberClickedListener) {
        this.memberClickedListener = memberClickedListener;
    }

    public MemberClickedListener getMemberClickedListener() {
        return memberClickedListener;
    }

    public List<ExternalMemberClickedListener> getExternalMemberClickedListeners() {
        return externalMemberClickedListeners;
    }

    public void addExternalMemberClickedListener(ExternalMemberClickedListener externalMemberClickedListener) {
        if (!externalMemberClickedListeners.contains(externalMemberClickedListener)) {
            externalMemberClickedListeners.add(externalMemberClickedListener);
        }
    }

    public void removeExternalMemberClickedListener(ExternalMemberClickedListener externalMemberClickedListener) {
        externalMemberClickedListeners.remove(externalMemberClickedListener);
    }

    /**
	 * Checks if the token at the specified offset is possibly a "click-able"
	 * region.
	 *
	 * @param textArea The text area.
	 * @param offs The offset, presumably at the mouse position.
	 * @return A result object.
	 */
	private IsLinkableCheckResult checkForLinkableToken(
			RSyntaxTextArea textArea, int offs) {

		IsLinkableCheckResult result = null;

		if (offs>=0) {

			try {

				int line = textArea.getLineOfOffset(offs);
				Token first = textArea.getTokenListForLine(line);
				RSyntaxDocument doc = (RSyntaxDocument)textArea.getDocument();
				Token prev = null;

				for (Token t=first; t!=null && t.isPaintable(); t=t.getNextToken()) {

					if (t.containsPosition(offs)) {

						// RSTA's tokens are pooled and re-used, so we must
						// defensively make a copy of the one we want to keep!
						Token token = new TokenImpl(t);
						boolean isMethod = false;

                        Token firstE = token;

                        int start=firstE.getOffset();
                        // get the whole line only if the prev token is dot
                        if (prev != null && prev.isSingleChar('.'))
                        {
                            String alreadyEnteredTextS2 = SourceCompletionProvider.getAlreadyEnteredTextS2(textArea, firstE.getOffset());
                            start = start - alreadyEnteredTextS2.length();
                        }

                        int end = token.getEndOffset();

                        // if next token is ( we need to find the corresponding closing ) token, and set the end to the closing )
                        if (t.getNextToken() != null && t.getNextToken().getType() != TokenTypes.NULL && t.getNextToken().isSingleChar('('))
                        {
                            int bcounter = 1;
                            // start from the token after the (
                            Token tmp = t.getNextToken().getNextToken();
                            while (tmp.getNextToken() != null && bcounter > 0) {
                                if (tmp.isSingleChar('(')) bcounter++;
                                if (tmp.isSingleChar(')')) bcounter--;
                                if (bcounter > 0) tmp = tmp.getNextToken();
                            }

                            // we managed to find the closing ) w
                            if (bcounter == 0 && tmp != null) {
                                end = tmp.getEndOffset();
                            }
                        }

//                        Token last= token;

//                        int maxLength = textArea.getText().length();
//                        String text2 = textArea.getText(end, Math.min(maxLength, 10));
//                        if(text2.trim().startsWith("(")) {
//                            int closeBracket = text2.indexOf(')');
//                            end+=closeBracket+1;
//                        }

                        IsLinkableCheckResult aa = new IsLinkableCheckResult(token, isMethod);
                        aa.start = start; // first.getOffset();
                        aa.end = end;
                        int length = aa.end - aa.start;
                        if(length<1) {
                        } else {
                            String text = textArea.getText(aa.start, length);
                            aa.text = text;
                            return aa;
                        }

//						if (prev==null) {
//							prev = RSyntaxUtilities.getPreviousImportantToken(
//									doc, line-1);
//						}
//						if (prev!=null && prev.isSingleChar('.')) {
//							// Not a field or method defined in this class.
//							break;
//						}
//
//						Token next = RSyntaxUtilities.getNextImportantToken(
//								t.getNextToken(), textArea, line);
//						if (next!=null && next.isSingleChar(Token.SEPARATOR, '(')) {
//							isMethod = true;
//						}
//
//						result = new IsLinkableCheckResult(token, isMethod);
//						break;

					}

//					else if (!t.isCommentOrWhitespace()) {
                    prev = t;
//					}

				}

			} catch (BadLocationException ble) {
				ble.printStackTrace(); // Never happens
			}

		}

		return result;

	}

	long lastAccess = -1;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LinkGeneratorResult isLinkAtOffset(final RSyntaxTextArea textArea,
			final int offs) {

		int start = -1;
		int end = -1;

		final IsLinkableCheckResult result = checkForLinkableToken(textArea, offs);
		if (result!=null) {

			JavaParser parser = jls.getParser(textArea);
			final CompilationUnit cu = parser.getCompilationUnit();
			Token t = result.token;
			boolean method = result.method;

			if (cu!=null) {

				final TypeDeclaration td = cu.getDeepestTypeDeclarationAtOffset(offs);
//				boolean staticFieldsOnly = false;
//				boolean deepestTypeDec = true;
//				boolean deepestContainingMemberStatic = false;
				if(td != null && start == -1) {
                    final Method findCurrentMethod = SourceCompletionProvider.findCurrentMethod(td, offs);
//                    if (findCurrentMethod != null) {
                        if(System.currentTimeMillis()-lastAccess < 2000) {
                            return null;
                        }
//                        Thread.dumpStack();
                        return new JavaRstaLinkGeneratorResult() {

                            @Override
                            public int getSourceOffset() {
//								log.info(1);
                                return 0;
                            }

                            @Override
                            public HyperlinkEvent execute() {
                                return executeWithCustomListener(memberClickedListener);
                            }

							@Override
							public HyperlinkEvent executeWithCustomListener(
									MemberClickedListener memberClickedListener2) {
                            	// log.info("memberClickedListener 1");
                                if (memberClickedListener != null) {
                                	// log.info("memberClickedListener 2");
                                    String text2 = result.text.replace("@", "");
                                    sourceCompletionProvider.open(cu, result.text, td, findCurrentMethod, text2, offs, offs - result.start, memberClickedListener2);
                                }
								return null;
							}
                        };
//                    } else {
//                    }
//					// First, check for a local variable in methods/static blocks
//					if (!method && deepestTypeDec) {
//
//						Iterator<Member> i = td.getMemberIterator();
//						while (i.hasNext()) {
//
//							Method m = null; // Nasty!  Clean this code up
//							Member member = i.next();
//							CodeBlock block = null;
//
//							// Check if a method or static block contains offs
//							if (member instanceof Method) {
//								m = (Method)member;
//								if (m.getBodyContainsOffset(offs) && m.getBody()!=null) {
//									deepestContainingMemberStatic = m.isStatic();
//									block = m.getBody().getDeepestCodeBlockContaining(offs);
//								}
//							}
//							else if (member instanceof CodeBlock) {
//								block = (CodeBlock)member;
//								deepestContainingMemberStatic = block.isStatic();
//								block = block.getDeepestCodeBlockContaining(offs);
//							}
//
//							// If so, scan its locals
//							if (block!=null) {
//								String varName = t.getLexeme();
//								// Local variables first, in reverse order
//								List<LocalVariable> locals = block.getLocalVarsBefore(offs);
//								Collections.reverse(locals);
//								for (LocalVariable local : locals) {
//									if (varName.equals(local.getName())) {
//										start = local.getNameStartOffset();
//										end = local.getNameEndOffset();
//									}
//								}
//								// Then arguments, if any.
//								if (start==-1 && m!=null) {
//									for (int j=0; j<m.getParameterCount(); j++) {
//										FormalParameter p = m.getParameter(j);
//										if (varName.equals(p.getName())) {
//											start = p.getNameStartOffset();
//											end = p.getNameEndOffset();
//										}
//									}
//								}
//								break; // No other code block will contain offs
//							}
//
//						}
//					}
//
//					// If no local var match, check fields or methods.
//					if (start==-1) {
//						String varName = t.getLexeme();
//						Iterator<? extends Member> i = method ?
//								td.getMethodIterator() : td.getFieldIterator();
//						while (i.hasNext()) {
//							Member member = i.next();
//							if (((!deepestContainingMemberStatic && !staticFieldsOnly) || member.isStatic()) &&
//									varName.equals(member.getName())) {
//								start = member.getNameStartOffset();
//								end = member.getNameEndOffset();
//								break;
//							}
//						}
//					}
//
//					// If still no match found, check parent type
//					if (start==-1) {
//						staticFieldsOnly |= td.isStatic();
//						//td = td.isStatic() ? null : td.getParentType();
//						td = td.getParentType();
//						// Don't check for local vars in parent type methods.
//						deepestTypeDec = false;
//					}

				}

			}

			if (start>-1) {
				return new SelectRegionLinkGeneratorResult(textArea, t.getOffset(),
						start, end);
			}

		}

		return null;

	}


	/**
	 * The result of checking whether a region of code under the mouse is
	 * <em>possibly</em> link-able.
	 */
	private static class IsLinkableCheckResult {

        public String text;
		/**
		 * The token under the mouse position.
		 */
		private Token token;

		private int start;
		private int end;

		/**
		 * Whether the token is a method invocation (as opposed to a local
		 * variable or field).
		 */
		private boolean method;

		private IsLinkableCheckResult(Token token, boolean method) {
			this.token = token;
			this.method = method;
		}

	}


}