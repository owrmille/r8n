import { motion } from "framer-motion";
import { MapPin, Clock, Download } from "lucide-react";
import { useParams, Link } from "react-router-dom";
import ReviewerAvatar from "@/components/ReviewerAvatar";
import OpinionListCard from "@/components/OpinionListCard";
import { Button } from "@/components/ui/button";

const CURRENT_USER_ID = "jane-doe";

const users: Record<string, {
  name: string;
  bio: string;
  reviewCount: number;
  listCount: number;
  location: string;
  memberSince: string;
}> = {
  "jane-doe": {
    name: "Jane Doe",
    bio: "Curious eater and honest reviewer. I care about quality, atmosphere, and whether a place lives up to the hype.",
    reviewCount: 12,
    listCount: 3,
    location: "Berlin, Germany",
    memberSince: "January 2025",
  },
  "alex-kruger": {
    name: "Alex Krüger",
    bio: "Berlin food explorer. Obsessed with finding the perfect bite — from Michelin-starred to street food.",
    reviewCount: 24,
    listCount: 5,
    location: "Berlin, Germany",
    memberSince: "March 2024",
  },
  "mia-svensson": {
    name: "Mia Svensson",
    bio: "Nordic cuisine enthusiast. Always searching for clean flavors and honest cooking.",
    reviewCount: 31,
    listCount: 4,
    location: "Berlin, Germany",
    memberSince: "June 2024",
  },
  "tobias-richter": {
    name: "Tobias Richter",
    bio: "Weekend explorer. Covering everything from street food markets to fine dining.",
    reviewCount: 22,
    listCount: 3,
    location: "Berlin, Germany",
    memberSince: "September 2024",
  },
  "sophie-chen": {
    name: "Sophie Chen",
    bio: "Coffee & dessert focused. If it's sweet or caffeinated, I've probably reviewed it.",
    reviewCount: 19,
    listCount: 2,
    location: "Berlin, Germany",
    memberSince: "November 2024",
  },
};

const myLists = [
  {
    title: "Best espresso in Berlin",
    description: "A curated guide to the city's finest espresso — from specialty roasters to hidden neighbourhood gems.",
    reviewCount: 12,
    authorName: "Jane Doe",
    hasAccess: true,
  },
  {
    title: "Date night restaurants",
    description: "Intimate, thoughtfully designed spaces where the food matches the ambiance.",
    reviewCount: 8,
    authorName: "Jane Doe",
    hasAccess: true,
  },
  {
    title: "Cheap lunch under €10",
    description: "Great food doesn't need to be expensive. Affordable lunch spots across Mitte and Kreuzberg.",
    reviewCount: 15,
    authorName: "Jane Doe",
    hasAccess: true,
  },
];

const otherUserLists = [
  {
    title: "Best espresso in Berlin",
    description: "A curated guide to the city's finest espresso — from specialty roasters to hidden neighbourhood gems.",
    reviewCount: 12,
    authorName: "Alex Krüger",
    hasAccess: false,
    accessStatus: "none" as const,
  },
  {
    title: "Date night restaurants",
    description: "Intimate, thoughtfully designed spaces where the food matches the ambiance.",
    reviewCount: 8,
    authorName: "Alex Krüger",
    hasAccess: true,
  },
  {
    title: "Weekend brunch spots",
    description: "Lazy Saturday mornings deserve great food. The best brunch places across Berlin.",
    reviewCount: 9,
    authorName: "Alex Krüger",
    hasAccess: false,
    accessStatus: "pending" as const,
  },
  {
    title: "Cheap lunch under €10",
    description: "Great food doesn't need to be expensive. Affordable lunch spots across Mitte and Kreuzberg.",
    reviewCount: 15,
    authorName: "Alex Krüger",
    hasAccess: false,
    accessStatus: "none" as const,
  },
  {
    title: "Top cocktail bars",
    description: "From speakeasies to rooftop bars — well-crafted drinks in the city.",
    reviewCount: 6,
    authorName: "Alex Krüger",
    hasAccess: true,
  },
];

const Profile = () => {
  const { id } = useParams();
  const isOwnProfile = !id || id === CURRENT_USER_ID;
  const profileUser = isOwnProfile ? users["jane-doe"] : (users[id!] || users["alex-kruger"]);

  return (
    <div className="mx-auto max-w-4xl px-4 py-8 md:px-8 md:py-12">
      {/* Profile header */}
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="mb-10"
      >
        <div className="flex flex-col sm:flex-row gap-6 items-start">
          <ReviewerAvatar name={profileUser.name} size="lg" />
          <div className="flex-1">
            <h1 className="text-2xl md:text-3xl font-semibold tracking-tight text-foreground mb-1">
              {profileUser.name}
            </h1>
            <p className="text-sm text-muted-foreground leading-relaxed mb-4 max-w-lg">
              {profileUser.bio}
            </p>

            <div className="flex flex-wrap gap-4 text-xs text-muted-foreground mb-4">
              <span className="flex items-center gap-1">
                <MapPin className="h-3 w-3" />
                {profileUser.location}
              </span>
              <span className="flex items-center gap-1">
                <Clock className="h-3 w-3" />
                Member since {profileUser.memberSince}
              </span>
            </div>

            <div className="flex gap-6 mb-6">
              <div className="text-center">
                <p className="text-lg font-mono font-semibold text-foreground">{profileUser.reviewCount}</p>
                <p className="text-[10px] text-muted-foreground uppercase tracking-wider">Reviews</p>
              </div>
              <div className="text-center">
                <p className="text-lg font-mono font-semibold text-foreground">{profileUser.listCount}</p>
                <p className="text-[10px] text-muted-foreground uppercase tracking-wider">Lists</p>
              </div>
            </div>

            {isOwnProfile && (
              <div className="flex gap-3">
                <Link to="/profile/edit">
                  <Button variant="outline" size="sm" className="rounded-xl">
                    Edit Profile
                  </Button>
                </Link>
                <Button
                  variant="outline"
                  size="sm"
                  className="rounded-xl gap-1.5"
                  onClick={() => {
                    const data = {
                      profile: profileUser,
                      lists: myLists,
                      exportedAt: new Date().toISOString(),
                    };
                    const blob = new Blob([JSON.stringify(data, null, 2)], { type: "application/json" });
                    const url = URL.createObjectURL(blob);
                    const a = document.createElement("a");
                    a.href = url;
                    a.download = "my-r8n-data.json";
                    a.click();
                    URL.revokeObjectURL(url);
                  }}
                >
                  <Download className="h-3.5 w-3.5" />
                  Export my data
                </Button>
              </div>
            )}
          </div>
        </div>
      </motion.div>

      {isOwnProfile ? (
        /* Own profile: show your lists */
        <motion.section
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: 0.15 }}
        >
          <h2 className="mb-4 text-lg font-semibold tracking-tight text-foreground">Your Lists</h2>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {myLists.map((list, i) => (
              <Link key={i} to={`/list/${list.title.toLowerCase().replace(/\s+/g, '-')}`}>
                <OpinionListCard {...list} />
              </Link>
            ))}
          </div>
        </motion.section>
      ) : (
        /* Other user: show their lists with access request */
        <motion.section
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: 0.15 }}
        >
          <h2 className="mb-4 text-lg font-semibold tracking-tight text-foreground">
            {profileUser.name}'s Lists
          </h2>
          <p className="text-sm text-muted-foreground mb-6">
            Request access to view individual lists and their reviews.
          </p>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {otherUserLists.map((list, i) => (
              <Link key={i} to={list.hasAccess ? `/list/${list.title.toLowerCase().replace(/\s+/g, '-')}` : "#"}>
                <OpinionListCard {...list} showAccessBadge />
              </Link>
            ))}
          </div>
        </motion.section>
      )}
    </div>
  );
};

export default Profile;
