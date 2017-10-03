package org.fife.rsta.ac.java.custom;


import org.fife.rsta.ac.java.ClassCompletion;
import org.fife.rsta.ac.java.JavaLanguageSupport;
import org.fife.rsta.ac.java.JavaLinkGenerator;
import org.fife.rsta.ac.java.MemberClickedListener;
import org.fife.rsta.ac.java.JavaLanguageSupport.ImportToAddInfo;
import org.fife.rsta.ac.java.classreader.ClassFile;
import org.fife.rsta.ac.java.classreader.FieldInfo;
import org.fife.rsta.ac.java.classreader.MethodInfo;
import org.fife.rsta.ac.java.rjc.ast.FormalParameter;
import org.fife.rsta.ac.java.rjc.ast.LocalVariable;
import org.fife.rsta.ac.java.rjc.ast.Method;
import org.fife.rsta.ac.java.rjc.ast.TypeDeclaration;
import org.fife.ui.rsyntaxtextarea.LinkGeneratorResult;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;


public class RSyntaxTextAreaCodeAssist extends  RSyntaxTextArea{
	private static final Logger log = Logger.getLogger(RSyntaxTextAreaCodeAssist.class.getName());

	private ArrayList<JMenuItem> menuItems = new ArrayList<JMenuItem>();

	private RSyntaxTextAreaCodeAssist textArea = this;

	public JavaLinkGenerator javaLinkGenerator;



	public JavaLanguageSupport groovyLanguageSupport;
	
	protected void appendFoldingMenu(javax.swing.JPopupMenu popupMenu) {
		super.appendFoldingMenu(popupMenu);
		log.info("createPopupMenu : " + menuItems.size());
		for (JMenuItem menuItem : menuItems) {
			popupMenu.add(menuItem);
		}
	};

	

	public void addMenuItem(JMenuItem menuItem) {
		menuItems.add(menuItem);
	}
	
	

    public static JPopupMenu getPopupMenuForMenuItem1(final Container component) {
        if (component instanceof JPopupMenu) {
            return (JPopupMenu) component;
        }
        return getPopupMenuForMenuItem1(component.getParent());
    }

	public static KeyStroke createVar = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
	
	public static KeyStroke gotoDeclaration = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);

	private int getInsertTextLocation(int lineStartOffsetOfCurrentLine) throws BadLocationException {
		String text = textArea.getText(lineStartOffsetOfCurrentLine, 1);
		if ("\t".equals(text)) {
			return getInsertTextLocation(lineStartOffsetOfCurrentLine + 1);
		}
		return lineStartOffsetOfCurrentLine;
	}
	
	public void addSupport() {
		javaLinkGenerator = (JavaLinkGenerator) textArea.getLinkGenerator();
		{
			textArea .addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent e) {
					// log.info(""+e);
					if (e.getKeyCode() == createVar.getKeyCode()) {
						createDecl();
					}
					if (e.getKeyCode() == gotoDeclaration.getKeyCode()) {
						gotToDeclaration();
					}
				}
			});
			{
				Action openDeclaration = new AbstractAction() {

					public void actionPerformed(ActionEvent e) {
//						int position = getPosition((JMenuItem)e.getSource());
//						textArea.setCaretPosition(position);
						createDecl();
					}

				};
				JMenuItem menuItem = new JMenuItem(openDeclaration);
				menuItem.setText("Create var");
				menuItem.setAccelerator(createVar);
				addMenuItem(menuItem);
				textArea.getActionMap().put(createVar, openDeclaration);
			}
			
			{
				Action openDeclaration = new AbstractAction() {

					public void actionPerformed(ActionEvent e) {
//						int position = getPosition((JMenuItem)e.getSource());
//						textArea.setCaretPosition(position);
						gotToDeclaration();
					}

				};
				JMenuItem menuItem = new JMenuItem(openDeclaration);
				menuItem.setText("Open declaration");
				menuItem.setAccelerator(gotoDeclaration);
				addMenuItem(menuItem);
				textArea.getActionMap().put(gotoDeclaration, openDeclaration);
			}

			// textArea.registerKeyboardAction(new ActionListener() {
			//
			// public void actionPerformed(ActionEvent e) {
			// gotToDeclaration();
			// }
			// }, gotoDeclaration, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		}
	}

	private void insertText(String shortName, String fullName) {
		try {
			int lineStartOffsetOfCurrentLine = textArea.getLineStartOffsetOfCurrentLine();
			lineStartOffsetOfCurrentLine = getInsertTextLocation(lineStartOffsetOfCurrentLine);
			if (fullName.contains("$")) {
				log.info(fullName + " not supported");
			} else {
				String insertText = shortName + " = ";
				textArea.insert(insertText, lineStartOffsetOfCurrentLine);
				textArea.setCaretPosition(lineStartOffsetOfCurrentLine + insertText.length() - 2);
				int lineEndOffsetOfCurrentLine = textArea.getLineEndOffsetOfCurrentLine();
				textArea.insert(";", lineEndOffsetOfCurrentLine - 1);
				if (fullName.contains(".")) {
					int i = fullName.indexOf("<");
					if (i > 0) {
						fullName = fullName.substring(0, i);
					}
					if(fullName.endsWith("[]")) {
						fullName = fullName.substring(0,fullName.length()-2);
					}
					ClassFile classFile = groovyLanguageSupport.getJarManager().getClassEntry(fullName);
					if(classFile==null) {
						log.info("clas not found "+fullName);
						String insertText2 = "import " + fullName + ";\n";
						textArea.insert(insertText2, 0);						
					}else {		
						ClassCompletion classCompletion = new ClassCompletion(null,classFile);
						ImportToAddInfo shouldAddImport = groovyLanguageSupport.ac.getShouldAddImport(classCompletion);
						log.info("should add "+shouldAddImport);
						if(shouldAddImport!=null) {
							textArea.insert(shouldAddImport.text, shouldAddImport.offs);
						}
					}
				}
			}
		} catch (Exception e) {
			log.log(Level.WARNING, fullName, e);
		}

	}

	void createDecl() {
		log.info("goto dec");
		int offset = textArea.getCaretPosition();
		log.info("offset = " + offset);
		LinkGeneratorResult res = javaLinkGenerator.isLinkAtOffset(textArea, offset);
		if (res == null) {
			log.info("no link to jump ");
		} else {
			if (res instanceof JavaRstaLinkGeneratorResult) {
				JavaRstaLinkGeneratorResult new_name = (JavaRstaLinkGeneratorResult) res;
				new_name.executeWithCustomListener(new MemberClickedListener() {

					public void openClass(String className) {

					}

					public void gotoMethodInClass(String className, MethodInfo methodInfo) {
						log.info("cp1");
						String returnTypeFull = methodInfo.getReturnTypeFull();
						log.info("full1 : " + returnTypeFull);
						log.info("full2 : " + methodInfo.getReturnTypeFull());
						String returnTypeShort = methodInfo.getReturnTypeString(false);
						insertText(returnTypeShort, returnTypeFull);
					}

					public void gotoFieldInClass(String className, FieldInfo fieldInfo) {
						String returnTypeFull = fieldInfo.getTypeString(true);
						String returnTypeShort = fieldInfo.getTypeString(false);
						insertText(returnTypeShort, returnTypeFull);
					}

					public void gotoMethodParameter(FormalParameter parameter) {
						log.info("cp1");

					}

					public void gotoMethod(Method method) {
						log.info("cp1");

					}

					public void gotoLocalVar(LocalVariable localVar) {
						log.info("cp1");

					}

					public void gotoInnerClass(TypeDeclaration typeDeclaration) {
						log.info("cp1");

					}

					public void gotoField(org.fife.rsta.ac.java.rjc.ast.Field field) {
						log.info("cp1");

					}
				});

			}else {
			log.info("no link shoup opned");
			}
		}

	}

	void gotToDeclaration() {
		log.info("goto dec");
		int offset = textArea.getCaretPosition();
		log.info("offset = " + offset);
		LinkGeneratorResult res = javaLinkGenerator.isLinkAtOffset(textArea, offset);
		if (res == null) {
			log.info("no link to jump ");
		} else {
			res.execute();
			log.info("no link shoup opned");
		}
	}
	
}
