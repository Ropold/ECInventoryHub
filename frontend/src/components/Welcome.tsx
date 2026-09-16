import welcomePic from '../assets/ec-logo.png';
import "./styles/Welcome.css"

export default function Welcome() {
    return (
        <>
            <h1>Welcome</h1>
            <h2>to The EC Inventory Hub</h2>
            <div className="image-wrapper margin-top-20">
                <img
                    src={welcomePic}
                    alt="Welcome to Ec Inventory Hub"
                    className="logo-welcome"
                />
            </div>
        </>
    )
}