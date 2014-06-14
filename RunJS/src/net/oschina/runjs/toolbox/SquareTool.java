package net.oschina.runjs.toolbox;

import java.util.List;

import net.oschina.runjs.beans.SquareCode;

public class SquareTool {
	public static List<SquareCode> listSquarecode(int page, int count) {
		return SquareCode.INSTANCE.listSquarecode(page, count);
	}

	public static List<SquareCode> listHotSquarecode(int page, int count) {
		return SquareCode.INSTANCE.listHotSquarecode(page, count);
	}

	public SquareCode getSquareCodeByIdent(String ident) {
		return SquareCode.INSTANCE.GetSquareCodeByIdent(ident);
	}

	public static boolean IsCodeInSquare(long cid) {
		return null != SquareCode.INSTANCE.GetSquareCodeByCode(cid);
	}

	public static SquareCode getSquareCodeById(long id) {
		return SquareCode.INSTANCE.Get(id);
	}

	public static boolean IsNewest(long scid) {
		SquareCode sc = SquareCode.INSTANCE.Get(scid);
		if (sc != null)
			return sc.IsNewest();
		return false;
	}

	public static int GetSquareCodeCount() {
		return SquareCode.INSTANCE.GetSquareCodeCount();
	}

	public static int HotSquareCount() {
		return SquareCode.INSTANCE.HotSquareCount();
	}
}