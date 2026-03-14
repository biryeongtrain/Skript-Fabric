package org.skriptlang.skript.log.runtime;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Handles filtering of runtime errors based on their level and predefined limits.
 */
public class RuntimeErrorFilter {

	public static final RuntimeErrorFilter NO_FILTER = new RuntimeErrorFilter(
			new Frame.FrameLimit(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE),
			new Frame.FrameLimit(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE)
		) {
			@Override
			public boolean test(@NotNull RuntimeError error) {
				return true;
			}
		};

	private Frame errorFrame, warningFrame;

	public RuntimeErrorFilter(Frame.FrameLimit errorFrameLimits, Frame.FrameLimit warningFrameLimits) {
		this.errorFrame = new Frame(errorFrameLimits);
		this.warningFrame = new Frame(warningFrameLimits);
	}

	public boolean test(@NotNull RuntimeError error) {
		return (error.level() == Level.SEVERE && errorFrame.add(error))
			|| (error.level() == Level.WARNING && warningFrame.add(error));
	}

	public void setErrorFrameLimits(Frame.FrameLimit limits) {
		this.errorFrame = new Frame(limits);
	}

	public void setWarningFrameLimits(Frame.FrameLimit limits) {
		this.warningFrame = new Frame(limits);
	}

	public Frame getErrorFrame() {
		return errorFrame;
	}

	public Frame getWarningFrame() {
		return warningFrame;
	}

}
