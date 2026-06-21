package de.leuc.adt.quickfix;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class QuickFixIcon {
    private static Image icon;

    public static Image get() {
        if (icon == null) {
            icon = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/qfs4c16.png")
                    .createImage();
        }
        return icon;
    }

}
