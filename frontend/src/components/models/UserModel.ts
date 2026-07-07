
export type Role = "VIEWER" | "USER" | "ADMIN";

export type UserModel = {
    id: string;
    githubId: string;
    username: string;
    name: string;
    avatarUrl: string;
    githubUrl: string;
    role: Role;
    preferredLanguage: string;
    createdAt: string;
    lastLoginAt: string;
};

export const DefaultUser: UserModel = {
    id: "0",
    githubId: "0",
    username: "Loading...",
    name: "Loading...",
    role: "VIEWER",
    preferredLanguage: "de",
    avatarUrl: "",
    githubUrl: "",
    createdAt: "Loading...",
    lastLoginAt: "Loading..."
}