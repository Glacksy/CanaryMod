import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class OServerConfigurationManager {

    public static Logger a = Logger.getLogger("Minecraft");
    public List b = new ArrayList();
    private OMinecraftServer c;
    // private OPlayerManager[] d = new OPlayerManager[3]; // CanaryMod: multiworld
    private int e;
    private Set f = new HashSet();
    private Set g = new HashSet();
    private Set h = new HashSet();
    private Set i = new HashSet();
    private File j;
    private File k;
    private File l;
    // private File m; //CanaryMod: disable Notchian whitelist
    // private OIPlayerFileData n; // CanaryMod: multiworld
    private boolean o;
    private int p = 0;
    
    private Map<String, OPlayerManager[]> managers = new HashMap<String, OPlayerManager[]>(1);
    private Map<String, OIPlayerFileData> saveHandlers = new HashMap<String, OIPlayerFileData>(1);
    private int viewDistance;

    public OServerConfigurationManager(OMinecraftServer var1) {
        // CanaryMod: initialize
        etc.setServer(var1);
        etc.getInstance().loadData();
        a.info("Note: your current classpath is: " + System.getProperty("java.class.path", "*UNKNOWN*"));
        if (!etc.getInstance().getTainted()) {
            a.info((etc.getInstance().isCrow()?"CanaryMod Crow":"CanaryMod")+" Build " + etc.getInstance().getVersionStr());
        } else {
            a.info("Tainted Build Information: " + etc.getInstance().getVersionStr());
        }
        
        this.c = var1;
        this.j = var1.a("banned-players.txt");
        this.k = var1.a("banned-ips.txt");
        this.l = var1.a("ops.txt");
        // this.m = var1.a("white-list.txt"); //CanaryMod: disable Notchian whitelist
        this.viewDistance = var1.d.a("view-distance", 10);
        
        this.e = var1.d.a("max-players", 20);
        this.o = var1.d.a("white-list", false);
        this.l();
        this.n();
        this.p();
        // this.r();
        this.m();
        this.o();
        this.q();
        // this.s();
    }

    public void a(OWorldServer[] var1) {
        this.saveHandlers.put(var1[0].name, var1[0].r().d());
    }

    public void a(OEntityPlayerMP var1) {
        OPlayerManager[] mgrs = this.managers.get(var1.bi.name);
        mgrs[0].b(var1);
        mgrs[1].b(var1);
        mgrs[2].b(var1);
        this.getManager(var1.bi.name, var1.w).removePlayer(var1);
        this.getManager(var1.bi.name, var1.w).addPlayer(var1);
        OWorldServer var2 = this.c.getWorld(var1.bi.name, var1.w);

        var2.G.c((int) var1.bm >> 4, (int) var1.bo >> 4);
    }

    public int a() {
        throw new UnsupportedOperationException("OServerConfigurationManager"
                + ".a() has been replaced by OServerConfigurationManager"
                + ".getMaxTrackingDistance(String).");
    }
    
    public int getMaxTrackingDistance(String worldName) {
        return this.managers.get(worldName)[0].c();
    }

    private OPlayerManager a(int var1) {
        throw new UnsupportedOperationException("OServerConfigurationManager.a"
                + "(int) has been replaced by OServerConfigurationManager.get"
                + "Manager(String, int).");
    }
    
    public PlayerManager getManager(String worldName, int dimension) {
        int index = dimension == -1 ? 1 : (dimension == 0 ? 0 : 2);
        return this.managers.get(worldName)[index].getCanaryPlayerManager();
    }

    public void b(OEntityPlayerMP var1) {
        this.saveHandlers.get(var1.bi.name).b(var1);
    }

    public void c(OEntityPlayerMP var1) {
        // CanaryMod: Playername with color and Prefix
        PlayerlistEntry entry = var1.getPlayer().getPlayerlistEntry(true);

        this.a(new OPacket201PlayerInfo(entry.getName(), entry.isShow(), 1000));
        this.b.add(var1);
        OWorldServer var2 = this.c.getWorld(var1.bi.name, var1.w);

        var2.G.c((int) var1.bm >> 4, (int) var1.bo >> 4);

        while (var2.a(var1, var1.bw).size() != 0) {
            var1.c(var1.bm, var1.bn + 1.0D, var1.bo);
        }

        var2.b(var1);
        this.getManager(var2.name, var1.w).addPlayer(var1);
        this.u();

        for (int var3 = 0; var3 < this.b.size(); ++var3) {
            OEntityPlayerMP var4 = (OEntityPlayerMP) this.b.get(var3);

            entry = var4.getPlayer().getPlayerlistEntry(true);
            var1.a.b(new OPacket201PlayerInfo(entry.getName(), entry.isShow(), entry.getPing()));
        }

        // CanaryMod: Handle login (send MOTD, send packet and set mode, and call hook)
        if (var1.getPlayer().getMode()) {
            var1.getPlayer().setCreativeMode(1);
        }
        etc.getInstance().getMotd(var1.getPlayer());
        etc.getLoader().callHook(PluginLoader.Hook.LOGIN, var1.getPlayer());
        var1.getPlayer().refreshCreativeMode();
    }

    public void d(OEntityPlayerMP var1) {
        this.getManager(var1.bi.name, var1.w).updateMountedMovingPlayer(var1);
    }

    public void e(OEntityPlayerMP var1) {
        this.saveHandlers.get(var1.bi.name).a(var1);
        this.c.getWorld(var1.bi.name, var1.w).e(var1);
        this.b.remove(var1);
        this.getManager(var1.bi.name, var1.w).removePlayer(var1);
        // CanaryMod: Player color and Prefix
        if (etc.getInstance().isPlayerList_enabled()) {
            PlayerlistEntry entry = var1.getPlayer().getPlayerlistEntry(false);

            this.a(new OPacket201PlayerInfo(entry.getName(), entry.isShow(), entry.getPing()));
        }
    }

    public OEntityPlayerMP a(ONetLoginHandler var1, String var2) {
        // TODO: add reasons, expire tempbans
        if (!etc.getLoader().isLoaded()) {
            var1.a("The server is not finished loading yet!");
        }
            
        // CanaryMod: whole section below is modified to handle whitelists etc
        OEntityPlayerMP temp = new OEntityPlayerMP(c, c.getWorld(c.m(), 0), var2,
                new OItemInWorldManager(c.getWorld(c.m(), 0)));
        Player player = temp.getPlayer();
        String ip = var1.b.c().toString();
        ip = ip.substring(ip.indexOf("/") + 1);
        ip = ip.substring(0, ip.lastIndexOf(":"));
        
        if (etc.getDataSource().isOnBanList(var2, ip)) {
            Ban ban = etc.getDataSource().getBan(var2, ip);
            var1.a(ban.getReason());
            return null;
        } else if (this.f.contains(var2.trim().toLowerCase())) {
            var1.a(etc.getInstance().getDefaultBanMessage());
            return null;
        } else if ((etc.getInstance().isWhitelistEnabled() && !(etc.getDataSource().isUserOnWhitelist(var2) || player.isAdmin()))) {
            var1.a(etc.getInstance().getWhitelistMessage());
            return null;
        } else {
            if (this.g.contains(ip)) {
                var1.a("Your IP address is banned from this server!");
                return null;
            } else if (this.b.size() >= this.e && !etc.getDataSource().isUserOnReserveList(var2)) {
                var1.a("The server is full!");
                return null;
            } else {
                if (player.getIps() != null && !player.getIps()[0].equals("")) {
                    boolean ipallowed = false;
                    for (String allowedip : player.getIps()) {
                        if (ip.equals(allowedip)) {
                            ipallowed = true;
                            break;
                        }
                    }
                    if (!ipallowed) {
                        var1.a("You are not allowed to log in from your current location");
                        return null;
                    }
                }

                for (int var4 = 0; var4 < this.b.size(); ++var4) {
                    OEntityPlayerMP var5 = (OEntityPlayerMP) this.b.get(var4);

                    if (var5.v.equalsIgnoreCase(var2)) {
                        if (etc.getInstance().isAltLocLoginAllowed()) {
                            var5.a.a("You logged in from another location");
                        } else {
                            var1.a("You are already logged in from another location");
                            return null;
                        }
                    }
                }

                // return new OEntityPlayerMP(this.c, this.c.a(0), var2, new OItemInWorldManager(this.c.a(0)));
            }
        }
        Object obj = etc.getLoader().callHook(PluginLoader.Hook.LOGINCHECK, var2, ip);

        if (obj instanceof String) {
            String result = (String) obj;

            if (result != null && !result.equals("")) {
                var1.a(result);
                return null;
            }
        }
        return temp;
    }
    
    // CanaryMod alias to set location when respawning.
    public OEntityPlayerMP a(OEntityPlayerMP var1, int var2, boolean var3) {
        return a(var1, var2, var3, null);
    }

    public OEntityPlayerMP a(OEntityPlayerMP var1, int var2, boolean var3, Location spawnLocation) {
        var1.bi.getEntityTracker().untrackPlayerSymmetrics(var1);
        var1.bi.getEntityTracker().untrackEntity(var1);
        this.b.remove(var1);
        var1.bi.world.removePlayerFromWorld(var1.getPlayer());
        this.getManager(var1.bi.name, var1.w).removePlayer(var1);
        this.c.getWorld(var1.bi.name, var1.w).f(var1);
        OChunkCoordinates var4 = var1.ab();

        var1.w = var2;
        OEntityPlayerMP var5 = new OEntityPlayerMP(this.c, var1.bi, var1.v, new OItemInWorldManager(var1.bi));

        if (var3) {
            var5.c((OEntityPlayer) var1);
        }

        var5.bd = var1.bd;
        var5.a = var1.a;
        OWorldServer var6 = (OWorldServer) var1.bi;

        var5.c.a(var1.c.a());
        var5.c.b(var6.s().m());
        if (var4 != null) {
            OChunkCoordinates var7 = OEntityPlayer.a(var1.bi, var4);

            if (var7 != null) {
                var5.c((double) ((float) var7.a + 0.5F), (double) ((float) var7.b + 0.1F), (double) ((float) var7.c + 0.5F), 0.0F, 0.0F);
                var5.a(var4);
            } else {
                var5.a.b((OPacket) (new OPacket70Bed(0, 0)));
            }
        }
        
        // CanaryMod set player location and angle if a spawn location is defined
        if (spawnLocation != null)
        {
            var5.c((double) spawnLocation.x, (double) spawnLocation.y, (double)spawnLocation.z, 0.0F, 0.0F);
        }

        var6.G.c((int) var5.bm >> 4, (int) var5.bo >> 4);

        while (var6.a(var5, var5.bw).size() != 0) {
            var5.c(var5.bm, var5.bn + 1.0D, var5.bo);
        }

        var5.a.b((OPacket) (new OPacket9Respawn(var5.w, (byte) var5.bi.q, var5.bi.s().p(), var5.bi.y(), var5.c.a())));
        var5.a.a(var5.bm, var5.bn, var5.bo, var5.bs, var5.bt);
        this.a(var5, var6);
        
        this.b.add(var5); //XXX
        var5.bi.getEntityTracker().trackEntity(var5);
        this.getManager(var5.bi.name, var5.w).addPlayer(var5);
        var5.bi.world.addPlayerToWorld(var5.getPlayer());
        var5.x();
        var5.E();
        return var5;
    }
    
    // CanaryMod alias to normally create portals when players are switching worlds.
    public void a(OEntityPlayerMP var1, int var2) {
        sendPlayerToOtherDimension(var1, var2, true);
    }

    // CanaryMod used to be a(OEntityPlayerMP var1, int var2) to teleport player to other dimensions.
    // Added createPortal option to cancel portal creation if not needed.
    public void sendPlayerToOtherDimension(OEntityPlayerMP var1, int var2, boolean createPortal) {
        int var3 = var1.w;
        OWorldServer var4 = this.c.getWorld(var1.bi.name, var1.w);

        var1.w = var2;
        OWorldServer var5 = this.c.getWorld(var1.bi.name, var1.w);

        var1.a.b((OPacket) (new OPacket9Respawn(var1.w, (byte) var1.bi.q, var5.s().p(), var5.y(), var1.c.a())));
        var4.f(var1);
        var1.bE = false;
        double var6 = var1.bm;
        double var8 = var1.bo;
        double var10 = 8.0D;

        if (var1.w == -1) {
            var6 /= var10;
            var8 /= var10;
            var1.c(var6, var1.bn, var8, var1.bs, var1.bt);
            if (var1.aE()) {
                var4.a(var1, false);
            }
        } else if (var1.w == 0) {
            var6 *= var10;
            var8 *= var10;
            var1.c(var6, var1.bn, var8, var1.bs, var1.bt);
            if (var1.aE()) {
                var4.a(var1, false);
            }
        } else {
            OChunkCoordinates var12 = var5.d();

            var6 = (double) var12.a;
            var1.bn = (double) var12.b;
            var8 = (double) var12.c;
            var1.c(var6, var1.bn, var8, 90.0F, 0.0F);
            if (var1.aE()) {
                var4.a(var1, false);
            }
        }

        if (var3 != 1 && var1.aE()) {
            var5.b(var1);
            var1.c(var6, var1.bn, var8, var1.bs, var1.bt);
            var5.a(var1, false);
            // CanaryMod - don't create portal if we are not using a portal to teleport.
            if (createPortal) {
                var5.G.a = true;
                (new OTeleporter()).a(var5, var1);
                var5.G.a = false;
            }
        }

        this.a(var1);
        var1.a.a(var1.bm, var1.bn, var1.bo, var1.bs, var1.bt);
        var1.a((OWorld) var5);
        var1.c.a(var5);
        this.a(var1, var5);
        this.f(var1);
    }

    public void b() {
        if ((etc.getInstance().isPlayerList_autoupdate()) && (this.p-- <= 0)) {
            for (int var1 = 0; var1 < this.b.size(); var1++) {
                OEntityPlayerMP var2 = (OEntityPlayerMP) this.b.get(var1);
                PlayerlistEntry entry = var2.getPlayer().getPlayerlistEntry(true);

                a(new OPacket201PlayerInfo(entry.getName(), entry.isShow(), entry.getPing()));
            }
            this.p = etc.getInstance().getPlayerList_ticks();
        }
        
        for (OPlayerManager[] mgrs : managers.values())
            for (int var2 = 0; var2 < mgrs.length; ++var2) {
                mgrs[var2].b();
            }

    }

    public void a(int var1, int var2, int var3, int var4) {
        throw new UnsupportedOperationException("OServerConfigurationManager"
                + ".a(int, int, int, int) has been replaced by OServer"
                + "ConfigurationManager.markBlockNeedsUpdate(int, int, int, int,"
                + " String).");
    }
    
    public void markBlockNeedsUpdate(int var1, int var2, int var3, int var4, String var5) {
        this.getManager(var5, var4).markBlockNeedsUpdate(var1, var2, var3);
    }

    /**
     * Send packet to all
     * @param var1 packet XXX
     */
    public void a(OPacket var1) {
        for (int var2 = 0; var2 < this.b.size(); ++var2) {
            OEntityPlayerMP var3 = (OEntityPlayerMP) this.b.get(var2);

            var3.a.b(var1);
        }

    }

    /**
     * Send packet to guys in dimension
     * @param var1 packet
     * @param var2 dimension id
     */
    public void a(OPacket var1, int var2) {
        for (int var3 = 0; var3 < this.b.size(); ++var3) {
            OEntityPlayerMP var4 = (OEntityPlayerMP) this.b.get(var3);

            if (var4.w == var2) {
                //CanaryMod re-route time updates to world-specific entity trackers
                var4.bi.getEntityTracker().sendPacketToPlayersAndEntity(var4, var1);
                //var4.a.b(var1);
            }
        }

    }

    public String c() {
        String var1 = "";

        for (int var2 = 0; var2 < this.b.size(); ++var2) {
            if (var2 > 0) {
                var1 = var1 + ", ";
            }

            var1 = var1 + ((OEntityPlayerMP) this.b.get(var2)).v;
        }

        return var1;
    }

    public String[] d() {
        String[] var1 = new String[this.b.size()];

        for (int var2 = 0; var2 < this.b.size(); ++var2) {
            var1[var2] = ((OEntityPlayerMP) this.b.get(var2)).v;
        }

        return var1;
    }

    public void a(String var1) {
        this.f.add(var1.toLowerCase());
        this.m();
    }

    public void b(String var1) {
        this.f.remove(var1.toLowerCase());
        this.m();
    }

    private void l() {
        try {
            this.f.clear();
            BufferedReader var1 = new BufferedReader(new FileReader(this.j));
            String var2 = "";

            while ((var2 = var1.readLine()) != null) {
                this.f.add(var2.trim().toLowerCase());
            }

            var1.close();
        } catch (Exception var3) {
            a.warning("Failed to load ban list: " + var3);
        }

    }

    private void m() {
        try {
            PrintWriter var1 = new PrintWriter(new FileWriter(this.j, false));
            Iterator var2 = this.f.iterator();

            while (var2.hasNext()) {
                String var3 = (String) var2.next();

                var1.println(var3);
            }

            var1.close();
        } catch (Exception var4) {
            a.warning("Failed to save ban list: " + var4);
        }

    }

    public Set e() {
        return this.f;
    }

    public Set f() {
        return this.g;
    }

    public void c(String var1) {
        this.g.add(var1.toLowerCase());
        this.o();
    }

    public void d(String var1) {
        this.g.remove(var1.toLowerCase());
        this.o();
    }

    private void n() {
        try {
            this.g.clear();
            BufferedReader var1 = new BufferedReader(new FileReader(this.k));
            String var2 = "";

            while ((var2 = var1.readLine()) != null) {
                this.g.add(var2.trim().toLowerCase());
            }

            var1.close();
        } catch (Exception var3) {
            a.warning("Failed to load ip ban list: " + var3);
        }

    }

    private void o() {
        try {
            PrintWriter var1 = new PrintWriter(new FileWriter(this.k, false));
            Iterator var2 = this.g.iterator();

            while (var2.hasNext()) {
                String var3 = (String) var2.next();

                var1.println(var3);
            }

            var1.close();
        } catch (Exception var4) {
            a.warning("Failed to save ip ban list: " + var4);
        }

    }

    public void e(String var1) {
        this.h.add(var1.toLowerCase());
        this.q();
    }

    public void f(String var1) {
        this.h.remove(var1.toLowerCase());
        this.q();
    }

    private void p() {
        try {
            this.h.clear();
            BufferedReader var1 = new BufferedReader(new FileReader(this.l));
            String var2 = "";

            while ((var2 = var1.readLine()) != null) {
                this.h.add(var2.trim().toLowerCase());
            }

            var1.close();
        } catch (Exception var3) {
            a.warning("Failed to load operators list: " + var3);
        }

    }

    private void q() {
        try {
            PrintWriter var1 = new PrintWriter(new FileWriter(this.l, false));
            Iterator var2 = this.h.iterator();

            while (var2.hasNext()) {
                String var3 = (String) var2.next();

                var1.println(var3);
            }

            var1.close();
        } catch (Exception var4) {
            a.warning("Failed to save operators list: " + var4);
        }

    }

    private void r() {
        // CanaryMod: Disable Notchian Whitelist
        /*try {
            this.i.clear();
            BufferedReader var1 = new BufferedReader(new FileReader(this.m));
            String var2 = "";

            while ((var2 = var1.readLine()) != null) {
                this.i.add(var2.trim().toLowerCase());
            }

            var1.close();
        } catch (Exception var3) {
            a.warning("Failed to load white-list: " + var3);
        }*/

    }

    private void s() {
        // CanaryMod: Disable Notchian Whitelist
        /*try {
            PrintWriter var1 = new PrintWriter(new FileWriter(this.m, false));
            Iterator var2 = this.i.iterator();

            while (var2.hasNext()) {
                String var3 = (String) var2.next();

                var1.println(var3);
            }

            var1.close();
        } catch (Exception var4) {
            a.warning("Failed to save white-list: " + var4);
        }*/

    }

    public boolean g(String var1) {
        var1 = var1.trim().toLowerCase();
        return !this.o || this.h.contains(var1) || this.i.contains(var1);
    }

    public boolean h(String var1) {
        return this.h.contains(var1.trim().toLowerCase());
    }

    public OEntityPlayerMP i(String var1) {
        for (int var2 = 0; var2 < this.b.size(); ++var2) {
            OEntityPlayerMP var3 = (OEntityPlayerMP) this.b.get(var2);

            if (var3.v.equalsIgnoreCase(var1)) {
                return var3;
            }
        }

        return null;
    }

    public void a(String var1, String var2) {
        OEntityPlayerMP var3 = this.i(var1);

        if (var3 != null) {
            var3.a.b((OPacket) (new OPacket3Chat(var2)));
        }

    }

    public void a(double var1, double var3, double var5, double var7, int var9, OPacket var10) {
        this.a((OEntityPlayer) null, var1, var3, var5, var7, var9, var10);
    }

    public void a(OEntityPlayer var1, double var2, double var4, double var6, double var8, int var10, OPacket var11) {
        for (int var12 = 0; var12 < this.b.size(); ++var12) {
            OEntityPlayerMP var13 = (OEntityPlayerMP) this.b.get(var12);

            if (var13 != var1 && var13.w == var10) {
                double var14 = var2 - var13.bm;
                double var16 = var4 - var13.bn;
                double var18 = var6 - var13.bo;

                if (var14 * var14 + var16 * var16 + var18 * var18 < var8 * var8) {
                    var13.a.b(var11);
                }
            }
        }

    }

    public void j(String var1) {
        OPacket3Chat var2 = new OPacket3Chat(var1);

        for (int var3 = 0; var3 < this.b.size(); ++var3) {
            OEntityPlayerMP var4 = (OEntityPlayerMP) this.b.get(var3);

            if (this.h(var4.v)) {
                var4.a.b((OPacket) var2);
            }
        }

    }

    public boolean a(String var1, OPacket var2) {
        OEntityPlayerMP var3 = this.i(var1);

        if (var3 != null) {
            var3.a.b(var2);
            return true;
        } else {
            return false;
        }
    }

    public void g() {
        for (int var1 = 0; var1 < this.b.size(); ++var1) {
            // CanaryMod: Store in a temp variable so we can get the save handler
            OEntityPlayer oep = (OEntityPlayer) this.b.get(var1);
            this.saveHandlers.get(oep.bi.name).a(oep);
        }

    }

    public void a(int var1, int var2, int var3, OTileEntity var4) {}

    public void k(String var1) {
        this.i.add(var1);
        // this.s(); CanaryMod - Disable Notchian whitelist
    }

    public void l(String var1) {
        this.i.remove(var1);
        // this.s(); CanaryMod - Disable Notchian whitelist
    }

    public Set h() {
        return this.i;
    }

    public void i() {
        // this.r(); CanaryMod - Disable Notchian whitelist
    }

    public void a(OEntityPlayerMP var1, OWorldServer var2) {
        var1.a.b((OPacket) (new OPacket4UpdateTime(var2.o())));
        if (var2.x()) {
            var1.a.b((OPacket) (new OPacket70Bed(1, 0)));
        }

    }

    public void f(OEntityPlayerMP var1) {
        var1.a(var1.l);
        var1.J();
    }

    public int j() {
        return this.b.size();
    }

    public int k() {
        return this.e;
    }

    public String[] t() {
        //CanaryMod get the default world from Canary world list instead.
        return etc.getServer().getDefaultWorld().getWorld().r().d().g();
       //return this.c.e[0].r().d().g();
    }

    private void u() {
       OPlayerUsageSnooper var1 = new OPlayerUsageSnooper("server");
       var1.a("version", this.c.i());
       var1.a("os_name", System.getProperty("os.name"));
       var1.a("os_version", System.getProperty("os.version"));
       var1.a("os_architecture", System.getProperty("os.arch"));
       var1.a("memory_total", Long.valueOf(Runtime.getRuntime().totalMemory()));
       var1.a("memory_max", Long.valueOf(Runtime.getRuntime().maxMemory()));
       var1.a("memory_free", Long.valueOf(Runtime.getRuntime().freeMemory()));
       var1.a("java_version", System.getProperty("java.version"));
       var1.a("cpu_cores", Integer.valueOf(Runtime.getRuntime().availableProcessors()));
       var1.a("players_current", Integer.valueOf(this.j()));
       var1.a("players_max", Integer.valueOf(this.k()));
       var1.a("players_seen", Integer.valueOf(this.t().length));
       var1.a("uses_auth", Boolean.valueOf(this.c.n));
       var1.a("server_brand", this.c.getServerModName());
       var1.a();
    }
    
    /**
     * Returns the list of bans
     * 
     * @return bans
     */
    public String getBans() {
        List<String> list = new ArrayList<String>(f);

        java.util.Collections.sort(list);
        StringBuilder builder = new StringBuilder();       
        int l = 0;

        for (String o : list) {
            if (l > 0) {
                builder.append(", ");
            }
            builder.append(o);
            l++;
        }
        return builder.toString();
    }

    /**
     * Returns the list of IP bans
     * 
     * @return ip bans
     */
    public String getIpBans() {
        StringBuilder builder = new StringBuilder();
        int l = 0;

        for (Object o : g) {
            if (l > 0) {
                builder.append(", ");
            }
            builder.append(o);
            l++;
        }
        return builder.toString();
    }

    /**
     * Returns player ban status
     * @param name player name
     * @return true if player is banned from server
     */
    public boolean isBanned(String name) {
        return this.f.contains(name.toLowerCase());
    }
    
    protected void newWorld(String name) {
        OPlayerManager[] toPut = new OPlayerManager[3];
        toPut[0] = new OPlayerManager(this.c, 0, this.viewDistance, name);
        toPut[1] = new OPlayerManager(this.c, -1, this.viewDistance, name);
        toPut[2] = new OPlayerManager(this.c, 1, this.viewDistance, name);
        this.managers.put(name, toPut);
    }
}
