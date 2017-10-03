package org.fife.rsta.ac.java.custom;

import javax.swing.event.HyperlinkEvent;

import org.fife.rsta.ac.java.MemberClickedListener;
import org.fife.ui.rsyntaxtextarea.LinkGeneratorResult;

public abstract class JavaRstaLinkGeneratorResult implements LinkGeneratorResult{

	public abstract HyperlinkEvent executeWithCustomListener(MemberClickedListener memberClickedListener) ;

}
