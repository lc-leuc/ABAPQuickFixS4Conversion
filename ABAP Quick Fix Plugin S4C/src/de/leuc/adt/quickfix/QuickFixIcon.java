package de.leuc.adt.quickfix;

import org.eclipse.swt.graphics.Image;

public class QuickFixIcon {
    private static Image icon;

    public static Image get() {
        if (icon == null) {
            icon = Activator.getDefault().imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/qfs4c16.png")
                    .createImage();
        }
        return icon;
    }

}
