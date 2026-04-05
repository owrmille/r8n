import { Home, Search, User, List, Bell, Settings, PenLine, ListPlus, LogOut } from "lucide-react";
import { useNavigate } from "react-router-dom";
import logo from "@/assets/logo.png";
import { NavLink } from "@/components/NavLink";
import { useLocation } from "react-router-dom";
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarFooter,
} from "@/components/ui/sidebar";
import { useSidebar } from "@/components/ui/use-sidebar";

const mainItems = [
  { title: "Dashboard", url: "/", icon: Home },
  { title: "Discover", url: "/discover", icon: Search },
  { title: "My Lists", url: "/lists", icon: List },
  { title: "Requests", url: "/requests", icon: Bell },
];

const secondaryItems = [
  { title: "Profile", url: "/profile", icon: User },
  { title: "Settings", url: "/settings", icon: Settings },
];

export function AppSidebar() {
  const { state } = useSidebar();
  const collapsed = state === "collapsed";
  const location = useLocation();
  const navigate = useNavigate();

  const isActive = (path: string) =>
    path === "/" ? location.pathname === "/" : location.pathname.startsWith(path);

  return (
    <Sidebar collapsible="icon" className="border-r border-sidebar-border">
      <SidebarContent className="pt-6">
        {/* Brand */}
        <div className={`mb-8 flex ${collapsed ? "justify-center px-1" : "px-5"}`}>
          {collapsed ? (
            <span className="text-lg font-bold text-sidebar-foreground tracking-tighter">R8N</span>
          ) : (
            <img src={logo} alt="R8N" className="h-12 w-auto object-contain" />
          )}
        </div>

        {/* Quick Actions */}
        {!collapsed && (
          <div className="px-4 mb-6 space-y-2">
            <NavLink
              to="/create"
              className="flex items-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
              activeClassName=""
            >
              <PenLine className="h-4 w-4" />
              Write Review
            </NavLink>
            <NavLink
              to="/lists/create"
              className="flex items-center gap-2 rounded-xl border border-border bg-card px-4 py-2.5 text-sm font-medium text-foreground transition-colors hover:bg-muted"
              activeClassName=""
            >
              <ListPlus className="h-4 w-4" />
              Create List
            </NavLink>
          </div>
        )}

        <SidebarGroup>
          <SidebarGroupLabel className="text-[10px] uppercase tracking-widest text-muted-foreground/60 px-6">
            Menu
          </SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {mainItems.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild>
                    <NavLink
                      to={item.url}
                      end={item.url === "/"}
                      className="mx-2 flex items-center gap-3 rounded-lg px-4 py-2 text-sm text-sidebar-foreground/70 transition-colors hover:bg-sidebar-accent"
                      activeClassName="bg-sidebar-accent text-sidebar-foreground font-medium"
                    >
                      <item.icon className="h-4 w-4 shrink-0" />
                      {!collapsed && <span>{item.title}</span>}
                      {item.title === "Requests" && !collapsed && (
                        <span className="ml-auto flex h-5 w-5 items-center justify-center rounded-full bg-accent text-[10px] font-mono font-semibold text-accent-foreground">
                          3
                        </span>
                      )}
                    </NavLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>

        <SidebarGroup className="mt-auto">
          <SidebarGroupLabel className="text-[10px] uppercase tracking-widest text-muted-foreground/60 px-6">
            Account
          </SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {secondaryItems.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild>
                    <NavLink
                      to={item.url}
                      className="mx-2 flex items-center gap-3 rounded-lg px-4 py-2 text-sm text-sidebar-foreground/70 transition-colors hover:bg-sidebar-accent"
                      activeClassName="bg-sidebar-accent text-sidebar-foreground font-medium"
                    >
                      <item.icon className="h-4 w-4 shrink-0" />
                      {!collapsed && <span>{item.title}</span>}
                    </NavLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>

      <SidebarFooter className="p-4 border-t border-sidebar-border">
        <div className="flex items-center gap-3">
          <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-primary-foreground text-xs font-medium shrink-0">
            JD
          </div>
          {!collapsed && (
            <>
              <div className="flex-1 min-w-0">
                <p className="truncate text-sm font-medium text-sidebar-foreground">Jane Doe</p>
                <p className="truncate text-xs text-muted-foreground">12 reviews</p>
              </div>
              <button
                onClick={() => navigate("/login")}
                className="shrink-0 rounded-lg p-1.5 text-muted-foreground/60 transition-colors hover:bg-sidebar-accent hover:text-foreground"
                title="Log out"
              >
                <LogOut className="h-4 w-4" />
              </button>
            </>
          )}
        </div>
      </SidebarFooter>
    </Sidebar>
  );
}
