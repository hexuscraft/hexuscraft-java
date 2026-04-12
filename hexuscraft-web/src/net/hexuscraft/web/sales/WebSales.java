package net.hexuscraft.web.sales;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.web.Web;
import net.hexuscraft.web.sales.command.CommandSales;

import java.util.Map;

public class WebSales extends MiniPlugin<Web>
{

    public enum PERM implements IPermission
    {
        COMMAND_SALES,
        COMMAND_SALES_RANK,
        COMMAND_SALES_RANK_ISSUE,
        COMMAND_SALES_RANK_REVOKE
    }

    public static final PermissionGroup[] STORE_RANKS = new PermissionGroup[]{PermissionGroup.VIP, PermissionGroup.MVP};

    CoreCommand _coreCommand;
    CoreDatabase _coreDatabase;

    public WebSales(Web webTranslator)
    {
        super(webTranslator, "Sales");
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies)
    {
        _coreCommand = (CoreCommand) dependencies.get(CoreCommand.class);
        _coreDatabase = (CoreDatabase) dependencies.get(CoreDatabase.class);
    }

    @Override
    public void onEnable()
    {
        _coreCommand.register(new CommandSales(this, _coreDatabase));
    }
}
