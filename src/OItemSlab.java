
public class OItemSlab extends OItemBlock {

    public OItemSlab(int var1) {
        super(var1);
        this.f(0);
        this.a(true);
    }

    public int a(int var1) {
        return var1;
    }

    public String a(OItemStack var1) {
        int var2 = var1.h();

        if (var2 < 0 || var2 >= OBlockStep.a.length) {
            var2 = 0;
        }

        return super.b() + "." + OBlockStep.a[var2];
    }

    public boolean a(OItemStack var1, OEntityPlayer var2, OWorld var3, int var4, int var5, int var6, int var7) {
        if ((Boolean) etc.getLoader().callHook(PluginLoader.Hook.ITEM_USE,
                ((OEntityPlayerMP) var2).getPlayer(), null,
                this.getBlockInfo(var3, var4, var5, var6, var7), new Item(var1)))
            return true;
            
        if (var1.a == 0) {
            return false;
        } else if (!var2.d(var4, var5, var6)) {
            return false;
        } else {
            int var8 = var3.a(var4, var5, var6);
            int var9 = var3.c(var4, var5, var6);
            int var10 = var9 & 7;
            boolean var11 = (var9 & 8) != 0;

            if ((var7 == 1 && !var11 || var7 == 0 && var11) && var8 == OBlock.ak.bO && var10 == var1.h()) {
                if (var3.a(OBlock.aj.e(var3, var4, var5, var6)) && var3.b(var4, var5, var6, OBlock.aj.bO, var10)) {
                    var3.a((double) ((float) var4 + 0.5F), (double) ((float) var5 + 0.5F), (double) ((float) var6 + 0.5F), OBlock.aj.cb.c(), (OBlock.aj.cb.a() + 1.0F) / 2.0F, OBlock.aj.cb.b() * 0.8F);
                    --var1.a;
                }

                return true;
            } else {
                return b(var1, var2, var3, var4, var5, var6, var7) ? true : super.a(var1, var2, var3, var4, var5, var6, var7);
            }
        }
    }

    private static boolean b(OItemStack var0, OEntityPlayer var1, OWorld var2, int var3, int var4, int var5, int var6) {
        if (var6 == 0) {
            --var4;
}

        if (var6 == 1) {
            ++var4;
        }

        if (var6 == 2) {
            --var5;
        }

        if (var6 == 3) {
            ++var5;
        }

        if (var6 == 4) {
            --var3;
        }

        if (var6 == 5) {
            ++var3;
        }

        int var7 = var2.a(var3, var4, var5);
        int var8 = var2.c(var3, var4, var5);
        int var9 = var8 & 7;

        if (var7 == OBlock.ak.bO && var9 == var0.h()) {
            if (var2.a(OBlock.aj.e(var2, var3, var4, var5)) && var2.b(var3, var4, var5, OBlock.aj.bO, var9)) {
                var2.a((double) ((float) var3 + 0.5F), (double) ((float) var4 + 0.5F), (double) ((float) var5 + 0.5F), OBlock.aj.cb.c(), (OBlock.aj.cb.a() + 1.0F) / 2.0F, OBlock.aj.cb.b() * 0.8F);
                --var0.a;
            }

            return true;
        } else {
            return false;
        }
    }
}
