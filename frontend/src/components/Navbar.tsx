import {useNavigate} from "react-router-dom";
import axios from "axios";
import "./styles/Navbar.css";
import companyLogo from "../assets/ec-logo.png";
import assignmentLogo from "../assets/assignment-logo.svg";
import laptopLogo from "../assets/laptop-logo.png";
import employeeLogo from "../assets/employee-logo.png";
import githubLogo from "../assets/github-logo.webp"
import worldLogo from "../assets/world-quartet-logo.jpg";
import userIcon from "../assets/user-icon.png";
import "./styles/Buttons.css";
import "./styles/Popup.css";

type NavbarProps = {
    user:string;
    getUser: () => void;
}

type NavItem = {
    path: string;
    logo: string;
    alt: string;
    title: string;
}

const PUBLIC_NAV_ITEMS: NavItem[] = [
    {path: "/", logo: companyLogo, alt: "Company Logo", title: "Home"},
    {path: "/employees", logo: employeeLogo, alt: "Employee Logo", title: "Employees"},
    {path: "/assignments", logo: assignmentLogo, alt: "Assignment Logo", title: "Assignments"},
    {path: "/devices", logo: laptopLogo, alt: "Laptop Logo", title: "Devices"},
    {path: "/locations", logo: worldLogo, alt: "World Logo", title: "Locations"},
];

const PROFILE_NAV_ITEM: NavItem = {path: "/profile", logo: userIcon, alt: "Profile Icon", title: "Profile"};

function loginWithGithub() {
    const host = window.location.host === "localhost:5173" ? "http://localhost:9876" : window.location.origin;
    window.open(host + "/oauth2/authorization/github", "_self");
}

export default function Navbar(props: Readonly<NavbarProps>)
{

    const navigate = useNavigate();

    const isLoggedIn = props.user !== "anonymousUser";
    const navItems = isLoggedIn ? [...PUBLIC_NAV_ITEMS, PROFILE_NAV_ITEM] : PUBLIC_NAV_ITEMS;

    function logoutFromGithub() {
        axios
            .post("/api/users/logout")
            .then(() => {
                props.getUser();
                navigate("/");
            })
            .catch((error) => {
                console.error("Logout failed:", error);
            });
    }

    return (
        <nav className="navbar">
            {navItems.map((item) => (
                <div
                    key={item.path}
                    className="clickable-header padding-left-5"
                    onClick={() => {
                        navigate(item.path);
                    }}
                >
                    <img src={item.logo} alt={item.alt} className="logo-image" />
                    <h2 className="header-title">{item.title}</h2>
                </div>
            ))}

            <button
                className="clickable-header padding-left-5"
                onClick={isLoggedIn ? logoutFromGithub : loginWithGithub}
            >
                <img src={githubLogo} alt="GitHub Logo" className="logo-image" />
                <h2 className="header-title">{isLoggedIn ? "Logout" : "Login GitHub"}</h2>
            </button>
        </nav>
    )
}
