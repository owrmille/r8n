import { Home, Search, Plus, List, User } from "lucide-react";
import { NavLink } from "@/components/NavLink";

const items = [
  { title: "Feed", url: "/", icon: Home },
  { title: "Discover", url: "/discover", icon: Search },
  { title: "Create", url: "/create", icon: Plus },
  { title: "Lists", url: "/lists", icon: List },
  { title: "Profile", url: "/profile", icon: User },
];

const MobileNav = () => {
  return (
    <nav className="fixed bottom-0 left-0 right-0 z-50 border-t border-border bg-background/95 backdrop-blur-sm md:hidden">
      <div className="flex items-center justify-around py-2">
        {items.map((item) => (
          <NavLink
            key={item.title}
            to={item.url}
            end={item.url === "/"}
            className="flex flex-col items-center gap-0.5 px-3 py-1.5 text-muted-foreground transition-colors"
            activeClassName="text-primary"
          >
            {item.title === "Create" ? (
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary text-primary-foreground -mt-4 shadow-card">
                <item.icon className="h-5 w-5" />
              </div>
            ) : (
              <item.icon className="h-5 w-5" />
            )}
            <span className="text-[10px] font-medium">{item.title}</span>
          </NavLink>
        ))}
      </div>
    </nav>
  );
};

export default MobileNav;
